package com.kama.chatmind.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillVO {
    private String id;

    private String name;

    private String description;

    private List<String> tools;

    private List<String> triggerKeywords;

    private String promptTemplate;
}
