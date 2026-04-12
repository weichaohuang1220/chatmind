package com.kama.chatmind.service.impl;

import com.kama.chatmind.service.MemoryCompressionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 记忆压缩服务实现 - 使用 LLM 将对话历史压缩为摘要
 */
@Slf4j
@Service
public class MemoryCompressionServiceImpl implements MemoryCompressionService {

    private static final String COMPRESSION_PROMPT = """
            请将以下对话历史压缩为简短摘要，保留关键信息（用户名、偏好、已确认的事实、重要问题和结论）：

            %s

            请用简洁的中文输出摘要，不要超过 500 字。只输出摘要内容，不要添加额外说明。
            """;

    @Override
    public String compressMemory(List<Message> oldMessages, ChatClient chatClient) {
        if (oldMessages == null || oldMessages.isEmpty()) {
            return null;
        }

        try {
            String conversationText = formatMessages(oldMessages);
            String prompt = COMPRESSION_PROMPT.formatted(conversationText);

            String summary = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("记忆压缩完成，原始消息数: {}，摘要长度: {}", oldMessages.size(),
                    summary != null ? summary.length() : 0);
            return summary;
        } catch (Exception e) {
            log.warn("记忆压缩失败，将跳过压缩继续运行: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将消息列表格式化为可读文本
     */
    private String formatMessages(List<Message> messages) {
        return messages.stream()
                .map(this::formatSingleMessage)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private String formatSingleMessage(Message message) {
        if (message instanceof SystemMessage systemMessage) {
            return "[系统]: " + systemMessage.getText();
        } else if (message instanceof UserMessage userMessage) {
            return "[用户]: " + userMessage.getText();
        } else if (message instanceof AssistantMessage assistantMessage) {
            String text = assistantMessage.getText();
            if (text == null || text.isBlank()) {
                // 纯工具调用的 assistant message，跳过或简要记录
                if (assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()) {
                    String toolNames = assistantMessage.getToolCalls().stream()
                            .map(AssistantMessage.ToolCall::name)
                            .collect(Collectors.joining(", "));
                    return "[助手调用工具]: " + toolNames;
                }
                return null;
            }
            return "[助手]: " + text;
        } else if (message instanceof ToolResponseMessage toolResponseMessage) {
            return toolResponseMessage.getResponses().stream()
                    .map(resp -> "[工具结果-" + resp.name() + "]: " + truncate(resp.responseData(), 200))
                    .collect(Collectors.joining("\n"));
        }
        return null;
    }

    /**
     * 截断过长的文本
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
