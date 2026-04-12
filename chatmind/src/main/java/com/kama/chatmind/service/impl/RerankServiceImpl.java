package com.kama.chatmind.service.impl;

import com.kama.chatmind.service.RerankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RerankServiceImpl implements RerankService {

    private static final Pattern INDEX_PATTERN = Pattern.compile("\\d+");

    @Override
    public List<String> rerank(String query, List<String> candidates, ChatClient chatClient, int topK) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        if (candidates.size() <= topK) {
            return new ArrayList<>(candidates);
        }

        try {
            // 构建编号列表
            StringBuilder chunksText = new StringBuilder();
            for (int i = 0; i < candidates.size(); i++) {
                chunksText.append(String.format("%d. %s\n", i + 1, candidates.get(i)));
            }

            String prompt = String.format("""
                    Given the query: '%s', rank the following text chunks by relevance.
                    Return ONLY the numbers of the most relevant chunks in order, separated by commas.
                    Return at most %d numbers.
                    Do not include any explanation, just the numbers.

                    Chunks:
                    %s""", query, topK, chunksText);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("[Rerank] LLM response: {}", response);

            // 解析 LLM 返回的编号
            List<Integer> indices = parseIndices(response, candidates.size());

            if (indices.isEmpty()) {
                log.warn("[Rerank] Failed to parse LLM response, falling back to original order");
                return candidates.subList(0, Math.min(topK, candidates.size()));
            }

            // 根据编号取出对应的文本块
            List<String> reranked = new ArrayList<>();
            for (int idx : indices) {
                if (reranked.size() >= topK) break;
                reranked.add(candidates.get(idx));
            }

            // 如果解析出的数量不足 topK，用原始顺序补充
            if (reranked.size() < topK) {
                for (String candidate : candidates) {
                    if (reranked.size() >= topK) break;
                    if (!reranked.contains(candidate)) {
                        reranked.add(candidate);
                    }
                }
            }

            log.info("[Rerank] Reranked {} candidates to top {}", candidates.size(), reranked.size());
            return reranked;

        } catch (Exception e) {
            log.warn("[Rerank] Reranking failed, falling back to original order", e);
            return candidates.subList(0, Math.min(topK, candidates.size()));
        }
    }

    /**
     * 解析 LLM 返回的编号字符串，如 "2, 1, 4" -> [1, 0, 3]（转为 0-based index）
     */
    private List<Integer> parseIndices(String response, int maxIndex) {
        List<Integer> indices = new ArrayList<>();
        if (response == null || response.isBlank()) {
            return indices;
        }

        Matcher matcher = INDEX_PATTERN.matcher(response);
        while (matcher.find()) {
            try {
                int num = Integer.parseInt(matcher.group());
                // LLM 返回的是 1-based，转为 0-based
                int zeroBasedIndex = num - 1;
                if (zeroBasedIndex >= 0 && zeroBasedIndex < maxIndex && !indices.contains(zeroBasedIndex)) {
                    indices.add(zeroBasedIndex);
                }
            } catch (NumberFormatException e) {
                // skip invalid numbers
            }
        }
        return indices;
    }
}
