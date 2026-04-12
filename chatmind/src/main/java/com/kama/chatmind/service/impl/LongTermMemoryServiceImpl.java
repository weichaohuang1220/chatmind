package com.kama.chatmind.service.impl;

import com.kama.chatmind.mapper.LongTermMemoryMapper;
import com.kama.chatmind.model.entity.LongTermMemory;
import com.kama.chatmind.service.LongTermMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 长期记忆服务实现 - 使用 LLM 从对话中提取关键信息并持久化
 */
@Slf4j
@Service
public class LongTermMemoryServiceImpl implements LongTermMemoryService {

    private static final String EXTRACTION_PROMPT = """
            从以下对话中提取关键信息（用户名、偏好、重要事实），以 key:value 格式输出，每行一条：

            %s

            注意事项：
            - 只提取明确的、有价值的信息，不要猜测
            - key 使用简短中文描述，如：用户名、喜欢的编程语言、工作领域
            - 如果没有可提取的信息，输出"无"
            - 不要添加额外说明，只输出 key:value 格式
            """;

    private final LongTermMemoryMapper longTermMemoryMapper;

    public LongTermMemoryServiceImpl(LongTermMemoryMapper longTermMemoryMapper) {
        this.longTermMemoryMapper = longTermMemoryMapper;
    }

    @Override
    public List<LongTermMemory> getMemories(String agentId) {
        return longTermMemoryMapper.selectByAgentId(agentId);
    }

    @Override
    public void saveMemory(String agentId, String chatSessionId, String key, String value, String category) {
        LongTermMemory memory = LongTermMemory.builder()
                .agentId(agentId)
                .chatSessionId(chatSessionId)
                .memoryKey(key)
                .memoryValue(value)
                .category(category != null ? category : "fact")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        longTermMemoryMapper.insert(memory);
    }

    @Override
    public void extractAndSaveMemories(String agentId, String chatSessionId, List<Message> messages, ChatClient chatClient) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        try {
            String conversationText = formatMessages(messages);
            if (conversationText.isBlank()) {
                return;
            }

            String prompt = EXTRACTION_PROMPT.formatted(conversationText);

            String result = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (result == null || result.isBlank() || result.trim().equals("无")) {
                log.info("[LongTermMemory] 本次对话无可提取的长期记忆");
                return;
            }

            // 解析 key:value 格式
            String[] lines = result.split("\n");
            int savedCount = 0;
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.equals("无")) {
                    continue;
                }

                int colonIdx = line.indexOf(':');
                if (colonIdx <= 0 || colonIdx >= line.length() - 1) {
                    // 尝试中文冒号
                    colonIdx = line.indexOf('\uff1a');
                }
                if (colonIdx <= 0 || colonIdx >= line.length() - 1) {
                    continue;
                }

                String key = line.substring(0, colonIdx).trim();
                String value = line.substring(colonIdx + 1).trim();

                if (!key.isEmpty() && !value.isEmpty()) {
                    // 根据 key 自动分类
                    String category = categorize(key);
                    saveMemory(agentId, chatSessionId, key, value, category);
                    savedCount++;
                }
            }

            log.info("[LongTermMemory] 从对话中提取并保存了 {} 条长期记忆, agentId={}", savedCount, agentId);
        } catch (Exception e) {
            log.warn("[LongTermMemory] 提取长期记忆失败，跳过: {}", e.getMessage());
        }
    }

    @Override
    public String formatMemoriesForPrompt(String agentId) {
        try {
            List<LongTermMemory> memories = getMemories(agentId);
            if (memories == null || memories.isEmpty()) {
                return null;
            }

            StringBuilder sb = new StringBuilder("[长期记忆]\n");
            for (LongTermMemory memory : memories) {
                sb.append("- ").append(memory.getMemoryKey())
                        .append(": ").append(memory.getMemoryValue())
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("[LongTermMemory] 格式化长期记忆失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 根据 key 名称自动分类
     */
    private String categorize(String key) {
        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("用户名") || lowerKey.contains("名字") || lowerKey.contains("姓名")
                || lowerKey.contains("name") || lowerKey.contains("偏好") || lowerKey.contains("喜欢")
                || lowerKey.contains("语言") || lowerKey.contains("preference")) {
            return "user_info";
        }
        if (lowerKey.contains("摘要") || lowerKey.contains("总结") || lowerKey.contains("summary")) {
            return "summary";
        }
        return "fact";
    }

    /**
     * 将消息列表格式化为可读文本（复用 MemoryCompressionService 的模式）
     */
    private String formatMessages(List<Message> messages) {
        return messages.stream()
                .map(this::formatSingleMessage)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private String formatSingleMessage(Message message) {
        if (message instanceof UserMessage userMessage) {
            return "[用户]: " + userMessage.getText();
        } else if (message instanceof AssistantMessage assistantMessage) {
            String text = assistantMessage.getText();
            if (text == null || text.isBlank()) {
                return null;
            }
            return "[助手]: " + text;
        }
        // 跳过 SystemMessage 和 ToolResponseMessage，不需要从中提取长期记忆
        return null;
    }
}
