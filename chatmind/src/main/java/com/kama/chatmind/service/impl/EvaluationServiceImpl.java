package com.kama.chatmind.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.model.dto.EvaluationReport;
import com.kama.chatmind.model.dto.EvaluationResult;
import com.kama.chatmind.service.EvaluationService;
import com.kama.chatmind.service.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EvaluationServiceImpl implements EvaluationService {

    private final RagService ragService;
    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;

    public EvaluationServiceImpl(RagService ragService,
                                 ObjectMapper objectMapper,
                                 @Qualifier("deepseek-chat") ChatClient chatClient) {
        this.ragService = ragService;
        this.objectMapper = objectMapper;
        this.chatClient = chatClient;
    }

    @Override
    public EvaluationResult evaluateRetrieval(String kbId, String question, List<String> expectedChunks) {
        // 调用 RAG 服务进行相似度搜索
        List<String> retrievedChunks = ragService.similaritySearch(kbId, question);

        // 检查检索结果中是否包含期望的关键词，同时记录首次命中的排名
        boolean hit = false;
        double score = 0.0;

        for (int i = 0; i < retrievedChunks.size(); i++) {
            String chunk = retrievedChunks.get(i);
            boolean chunkHit = expectedChunks.stream().anyMatch(chunk::contains);
            if (chunkHit && !hit) {
                hit = true;
                // MRR score: 1 / (rank), rank is 1-indexed
                score = 1.0 / (i + 1);
            }
        }

        return EvaluationResult.builder()
                .question(question)
                .hit(hit)
                .score(score)
                .retrievedChunks(retrievedChunks)
                .build();
    }

    @Override
    public EvaluationReport runFullEvaluation(String kbId) {
        List<Map<String, Object>> dataset = loadDataset();
        List<EvaluationResult> results = new ArrayList<>();
        int hitCount = 0;
        double mrrSum = 0.0;

        for (Map<String, Object> entry : dataset) {
            String question = (String) entry.get("question");
            @SuppressWarnings("unchecked")
            List<String> expectedChunks = (List<String>) entry.get("expectedChunks");

            if (question == null || expectedChunks == null) {
                log.warn("跳过缺少 question 或 expectedChunks 的数据集条目: {}", entry);
                continue;
            }

            EvaluationResult result = evaluateRetrieval(kbId, question, expectedChunks);
            results.add(result);

            if (result.isHit()) {
                hitCount++;
                mrrSum += result.getScore();
            }
        }

        int total = results.size();
        double recall = total > 0 ? (double) hitCount / total : 0.0;
        double mrr = total > 0 ? mrrSum / total : 0.0;

        return EvaluationReport.builder()
                .totalQuestions(total)
                .recall(recall)
                .mrr(mrr)
                .results(results)
                .build();
    }

    @Override
    public EvaluationReport runFullEvaluationWithJudge(String kbId) {
        List<Map<String, Object>> dataset = loadDataset();
        List<EvaluationResult> results = new ArrayList<>();
        int hitCount = 0;
        double mrrSum = 0.0;
        double llmScoreSum = 0.0;
        int llmScoreCount = 0;

        for (Map<String, Object> entry : dataset) {
            String question = (String) entry.get("question");
            @SuppressWarnings("unchecked")
            List<String> expectedChunks = (List<String>) entry.get("expectedChunks");
            String expectedAnswer = (String) entry.get("expectedAnswer");

            if (question == null || expectedChunks == null || expectedAnswer == null) {
                log.warn("跳过缺少必要字段的数据集条目: {}", entry);
                continue;
            }

            EvaluationResult result = evaluateRetrieval(kbId, question, expectedChunks);

            // LLM-as-Judge 评估
            String retrievedContent = String.join("\n", result.getRetrievedChunks());
            try {
                String llmResponse = callLlmJudge(question, expectedAnswer, retrievedContent);
                double llmScore = parseScore(llmResponse);
                String llmReason = parseReason(llmResponse);
                result.setLlmJudgeScore(llmScore);
                result.setLlmJudgeReason(llmReason);
                llmScoreSum += llmScore;
                llmScoreCount++;
            } catch (Exception e) {
                log.warn("LLM-as-Judge 评估失败，问题: {}，原因: {}", question, e.getMessage());
                result.setLlmJudgeScore(null);
                result.setLlmJudgeReason("评估失败: " + e.getMessage());
            }

            results.add(result);

            if (result.isHit()) {
                hitCount++;
                mrrSum += result.getScore();
            }
        }

        int total = results.size();
        double recall = total > 0 ? (double) hitCount / total : 0.0;
        double mrr = total > 0 ? mrrSum / total : 0.0;
        Double averageLlmScore = llmScoreCount > 0 ? llmScoreSum / llmScoreCount : null;

        return EvaluationReport.builder()
                .totalQuestions(total)
                .recall(recall)
                .mrr(mrr)
                .results(results)
                .averageLlmScore(averageLlmScore)
                .build();
    }

    /**
     * 调用 LLM 作为评判者，评估检索结果是否能回答用户的问题
     *
     * @param question         用户问题
     * @param expectedAnswer   期望答案的关键词（正则模式）
     * @param retrievedContent 检索到的内容
     * @return LLM 原始响应文本
     */
    private String callLlmJudge(String question, String expectedAnswer, String retrievedContent) {
        String prompt = String.format(
                "请评估以下检索结果是否能回答用户的问题。\n" +
                "问题：%s\n" +
                "期望答案包含：%s\n" +
                "检索到的内容：%s\n" +
                "请给出0到1的分数（1=完全相关，0=完全无关），格式：分数:理由",
                question, expectedAnswer, retrievedContent
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 从 LLM Judge 响应中解析分数
     */
    private double parseScore(String response) {
        if (response == null || response.isBlank()) {
            log.warn("LLM Judge 返回空响应");
            return 0.0;
        }

        try {
            String trimmed = response.trim();
            int colonIndex = trimmed.indexOf(':');
            if (colonIndex == -1) {
                colonIndex = trimmed.indexOf('：');
            }

            if (colonIndex > 0) {
                String scorePart = trimmed.substring(0, colonIndex).trim();
                String numStr = scorePart.replaceAll("[^0-9.]", "");
                if (!numStr.isEmpty()) {
                    double score = Double.parseDouble(numStr);
                    return Math.max(0.0, Math.min(1.0, score));
                }
            }

            // 回退：从整个响应中提取第一个数字
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)").matcher(trimmed);
            if (matcher.find()) {
                double score = Double.parseDouble(matcher.group(1));
                return Math.max(0.0, Math.min(1.0, score));
            }
        } catch (NumberFormatException e) {
            log.warn("无法解析 LLM Judge 分数，响应: {}", response);
        }

        return 0.0;
    }

    /**
     * 从 LLM Judge 响应中解析理由
     */
    private String parseReason(String response) {
        if (response == null || response.isBlank()) {
            return "";
        }

        String trimmed = response.trim();
        int colonIndex = trimmed.indexOf(':');
        if (colonIndex == -1) {
            colonIndex = trimmed.indexOf('：');
        }

        if (colonIndex >= 0 && colonIndex < trimmed.length() - 1) {
            return trimmed.substring(colonIndex + 1).trim();
        }

        return trimmed;
    }

    private List<Map<String, Object>> loadDataset() {
        try {
            ClassPathResource resource = new ClassPathResource("evaluation/qa-dataset.json");
            try (InputStream inputStream = resource.getInputStream()) {
                return objectMapper.readValue(inputStream, new TypeReference<>() {});
            }
        } catch (IOException e) {
            log.error("加载评估数据集失败", e);
            throw new RuntimeException("加载评估数据集失败", e);
        }
    }
}
