package com.kama.chatmind.service;

import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

public interface RerankService {
    /**
     * 使用 LLM 对候选文本块进行重排序，返回最相关的 topK 个结果。
     * 如果重排序失败，返回原始顺序的前 topK 个结果（优雅降级）。
     *
     * @param query      用户查询
     * @param candidates 候选文本块列表
     * @param chatClient 用于调用 LLM 的 ChatClient
     * @param topK       返回的最大结果数
     * @return 重排序后的文本块列表
     */
    List<String> rerank(String query, List<String> candidates, ChatClient chatClient, int topK);
}
