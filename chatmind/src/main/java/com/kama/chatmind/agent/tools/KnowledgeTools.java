package com.kama.chatmind.agent.tools;

import com.kama.chatmind.service.RagService;
import com.kama.chatmind.service.RerankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class KnowledgeTools implements Tool {

    private final RagService ragService;
    private final RerankService rerankService;
    private final ChatClient chatClient;

    // 初始检索数量（扩大召回）
    private static final int RETRIEVAL_TOP_K = 5;
    // 重排序后保留数量
    private static final int RERANK_TOP_K = 3;

    public KnowledgeTools(RagService ragService, RerankService rerankService,
                          @Qualifier("deepseek-chat") ChatClient chatClient) {
        this.ragService = ragService;
        this.rerankService = rerankService;
        this.chatClient = chatClient;
    }

    @Override
    public String getName() {
        return "KnowledgeTool";
    }

    @Override
    public String getDescription() {
        return "用于从知识库执行语义检索（RAG）。输入知识库 ID 和查询文本，返回与查询最相关的内容片段。";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "KnowledgeTool",
            description = "从指定知识库中执行相似性检索（RAG）。参数为知识库 ID（kbsId）和查询文本（query），返回与查询最相关的知识片段。"
    )
    public String knowledgeQuery(String kbsId, String query) {
        // 先检索 top 5 候选
        List<String> candidates = ragService.similaritySearch(kbsId, query, RETRIEVAL_TOP_K);

        // 使用 LLM 重排序，选出 top 3
        List<String> reranked;
        try {
            reranked = rerankService.rerank(query, candidates, chatClient, RERANK_TOP_K);
            log.info("[KnowledgeTools] Reranked {} candidates to {} results", candidates.size(), reranked.size());
        } catch (Exception e) {
            log.warn("[KnowledgeTools] Rerank failed, using original results", e);
            reranked = candidates.subList(0, Math.min(RERANK_TOP_K, candidates.size()));
        }

        return String.join("\n", reranked);
    }
}
