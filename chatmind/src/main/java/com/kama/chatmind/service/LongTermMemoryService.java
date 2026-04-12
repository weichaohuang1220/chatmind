package com.kama.chatmind.service;

import com.kama.chatmind.model.entity.LongTermMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 长期记忆服务 - 跨会话持久化用户关键信息（用户名、偏好、确认的事实等）
 */
public interface LongTermMemoryService {

    /**
     * 获取指定 Agent 的所有长期记忆
     */
    List<LongTermMemory> getMemories(String agentId);

    /**
     * 保存单条长期记忆（如果 key 已存在则更新）
     */
    void saveMemory(String agentId, String chatSessionId, String key, String value, String category);

    /**
     * 从对话消息中提取关键信息并持久化为长期记忆
     *
     * @param agentId       智能体 ID
     * @param chatSessionId 会话 ID
     * @param messages      本次会话的消息列表
     * @param chatClient    用于调用 LLM 提取信息的 ChatClient
     */
    void extractAndSaveMemories(String agentId, String chatSessionId, List<Message> messages, ChatClient chatClient);

    /**
     * 将长期记忆格式化为可注入系统提示词的文本
     *
     * @param agentId 智能体 ID
     * @return 格式化的记忆文本，无记忆时返回 null
     */
    String formatMemoriesForPrompt(String agentId);
}
