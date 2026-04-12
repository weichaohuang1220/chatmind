package com.kama.chatmind.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.model.request.CreateSkillRequest;
import com.kama.chatmind.model.request.UpdateSkillRequest;
import com.kama.chatmind.model.response.CreateSkillResponse;
import com.kama.chatmind.model.response.GetSkillsResponse;
import com.kama.chatmind.model.vo.SkillVO;
import com.kama.chatmind.service.SkillFacadeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkillController.class)
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SkillFacadeService skillFacadeService;

    @Test
    void getSkills_success() throws Exception {
        SkillVO skill = new SkillVO("skill-1", "Web Search", "Search the web",
                List.of("WebSearchTool"), List.of("search", "find"), null);

        GetSkillsResponse response = GetSkillsResponse.builder()
                .skills(new SkillVO[]{skill}).build();
        when(skillFacadeService.getSkills()).thenReturn(response);

        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.skills[0].id").value("skill-1"))
                .andExpect(jsonPath("$.data.skills[0].name").value("Web Search"));
    }

    @Test
    void getSkills_empty() throws Exception {
        GetSkillsResponse response = GetSkillsResponse.builder()
                .skills(new SkillVO[]{}).build();
        when(skillFacadeService.getSkills()).thenReturn(response);

        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skills").isEmpty());
    }

    @Test
    void getSkills_serviceThrows() throws Exception {
        when(skillFacadeService.getSkills()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void getSkillById_success() throws Exception {
        SkillVO skill = new SkillVO("skill-1", "Web Search", "desc",
                List.of("WebSearchTool"), List.of(), null);
        when(skillFacadeService.getSkillById("skill-1")).thenReturn(skill);

        mockMvc.perform(get("/api/skills/skill-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("skill-1"))
                .andExpect(jsonPath("$.data.name").value("Web Search"));
    }

    @Test
    void getSkillById_serviceThrows() throws Exception {
        when(skillFacadeService.getSkillById("bad-id")).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/skills/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void createSkill_success() throws Exception {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("New Skill");
        request.setDescription("desc");
        request.setTools(List.of("ToolA", "ToolB"));
        request.setTriggerKeywords(List.of("keyword1"));
        request.setPromptTemplate("You are a helpful assistant");

        CreateSkillResponse response = CreateSkillResponse.builder()
                .skillId("skill-new").build();
        when(skillFacadeService.createSkill(any())).thenReturn(response);

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.skillId").value("skill-new"));
    }

    @Test
    void createSkill_serviceThrows() throws Exception {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Bad Skill");
        when(skillFacadeService.createSkill(any())).thenThrow(new RuntimeException("Failed"));

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void updateSkill_success() throws Exception {
        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setName("Updated Skill");
        doNothing().when(skillFacadeService).updateSkill(eq("skill-1"), any());

        mockMvc.perform(patch("/api/skills/skill-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(skillFacadeService).updateSkill(eq("skill-1"), any());
    }

    @Test
    void updateSkill_serviceThrows() throws Exception {
        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setName("Bad");
        doThrow(new RuntimeException("Failed")).when(skillFacadeService).updateSkill(eq("bad-id"), any());

        mockMvc.perform(patch("/api/skills/bad-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void deleteSkill_success() throws Exception {
        doNothing().when(skillFacadeService).deleteSkill("skill-1");

        mockMvc.perform(delete("/api/skills/skill-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(skillFacadeService).deleteSkill("skill-1");
    }

    @Test
    void deleteSkill_serviceThrows() throws Exception {
        doThrow(new RuntimeException("Not found")).when(skillFacadeService).deleteSkill("bad-id");

        mockMvc.perform(delete("/api/skills/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
