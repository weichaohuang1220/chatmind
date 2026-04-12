package com.kama.chatmind.model.vo;

import com.kama.chatmind.model.dto.AgentDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AgentVO {
    private String id;

    private String name;

    private String description;

    private String systemPrompt;

    private AgentDTO.ModelType model;

    private List<String> allowedTools;

    private List<String> allowedKbs;

    private AgentDTO.ChatOptions chatOptions;

    private List<String> allowedSkills;
}
