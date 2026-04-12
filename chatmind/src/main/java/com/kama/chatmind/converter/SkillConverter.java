package com.kama.chatmind.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.model.dto.SkillDTO;
import com.kama.chatmind.model.entity.Skill;
import com.kama.chatmind.model.request.CreateSkillRequest;
import com.kama.chatmind.model.request.UpdateSkillRequest;
import com.kama.chatmind.model.vo.SkillVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
@AllArgsConstructor
public class SkillConverter {

    private final ObjectMapper objectMapper;

    public Skill toEntity(SkillDTO skillDTO) throws JsonProcessingException {
        Assert.notNull(skillDTO, "SkillDTO cannot be null");
        Assert.notNull(skillDTO.getTools(), "Tools cannot be null");

        return Skill.builder()
                .id(skillDTO.getId())
                .name(skillDTO.getName())
                .description(skillDTO.getDescription())
                .tools(objectMapper.writeValueAsString(skillDTO.getTools()))
                .triggerKeywords(skillDTO.getTriggerKeywords() != null
                        ? objectMapper.writeValueAsString(skillDTO.getTriggerKeywords())
                        : "[]")
                .promptTemplate(skillDTO.getPromptTemplate())
                .createdAt(skillDTO.getCreatedAt())
                .updatedAt(skillDTO.getUpdatedAt())
                .build();
    }

    public SkillDTO toDTO(Skill skill) throws JsonProcessingException {
        Assert.notNull(skill, "Skill cannot be null");
        Assert.notNull(skill.getTools(), "Tools cannot be null");

        return SkillDTO.builder()
                .id(skill.getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .tools(objectMapper.readValue(skill.getTools(), new TypeReference<List<String>>(){}))
                .triggerKeywords(skill.getTriggerKeywords() != null
                        ? objectMapper.readValue(skill.getTriggerKeywords(), new TypeReference<List<String>>(){})
                        : List.of())
                .promptTemplate(skill.getPromptTemplate())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }

    public SkillVO toVO(SkillDTO dto) {
        return SkillVO.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .tools(dto.getTools())
                .triggerKeywords(dto.getTriggerKeywords())
                .promptTemplate(dto.getPromptTemplate())
                .build();
    }

    public SkillVO toVO(Skill skill) throws JsonProcessingException {
        return toVO(toDTO(skill));
    }

    public SkillDTO toDTO(CreateSkillRequest request) {
        Assert.notNull(request, "CreateSkillRequest cannot be null");
        Assert.notNull(request.getTools(), "Tools cannot be null");

        return SkillDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .tools(request.getTools())
                .triggerKeywords(request.getTriggerKeywords() != null ? request.getTriggerKeywords() : List.of())
                .promptTemplate(request.getPromptTemplate())
                .build();
    }

    public void updateDTOFromRequest(SkillDTO dto, UpdateSkillRequest request) {
        Assert.notNull(dto, "SkillDTO cannot be null");
        Assert.notNull(request, "UpdateSkillRequest cannot be null");

        if (request.getName() != null) {
            dto.setName(request.getName());
        }
        if (request.getDescription() != null) {
            dto.setDescription(request.getDescription());
        }
        if (request.getTools() != null) {
            dto.setTools(request.getTools());
        }
        if (request.getTriggerKeywords() != null) {
            dto.setTriggerKeywords(request.getTriggerKeywords());
        }
        if (request.getPromptTemplate() != null) {
            dto.setPromptTemplate(request.getPromptTemplate());
        }
    }
}
