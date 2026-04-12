package com.kama.chatmind.model.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName skill
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill {
    private String id;

    private String name;

    private String description;

    // JSON String
    private String tools;

    // JSON String
    private String triggerKeywords;

    private String promptTemplate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
