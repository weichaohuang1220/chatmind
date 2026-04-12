package com.kama.chatmind.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 记忆压缩服务 - 将过长的对话历史压缩为摘要
 */
public interface MemoryCompressionService {

    /**
     * 将旧的对话消息压缩为简短摘要
     *
     * @param oldMessages 需要压缩的旧消息列表
     * @param chatClient  用于调用 LLM 生成摘要的 ChatClient
     * @return 压缩后的摘要文本，如果压缩失败则返回 null
     */
    String compressMemory(List<Message> oldMessages, ChatClient chatClient);
}
