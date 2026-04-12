package com.kama.chatmind.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO {
    private String id;

    private String name;

    private String description;

    private List<String> tools;

    private List<String> triggerKeywords;

    private String promptTemplate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
