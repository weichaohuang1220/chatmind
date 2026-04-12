package com.kama.chatmind.model.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateSkillRequest {
    private String name;
    private String description;
    private List<String> tools;
    private List<String> triggerKeywords;
    private String promptTemplate;
}
