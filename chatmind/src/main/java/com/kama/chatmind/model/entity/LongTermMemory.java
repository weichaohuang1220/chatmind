package com.kama.chatmind.model.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * 长期记忆实体 - 跨会话持久化的用户关键信息
 * @TableName long_term_memory
 */
@Data
@Builder
public class LongTermMemory {
    private String id;

    private String agentId;

    private String chatSessionId;

    private String memoryKey;

    private String memoryValue;

    private String category;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
