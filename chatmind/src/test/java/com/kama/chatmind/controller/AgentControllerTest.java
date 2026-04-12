package com.kama.chatmind.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.model.dto.AgentDTO;
import com.kama.chatmind.model.request.CreateAgentRequest;
import com.kama.chatmind.model.request.UpdateAgentRequest;
import com.kama.chatmind.model.response.CreateAgentResponse;
import com.kama.chatmind.model.response.GetAgentsResponse;
import com.kama.chatmind.model.vo.AgentVO;
import com.kama.chatmind.service.AgentFacadeService;
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

@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AgentFacadeService agentFacadeService;

    @Test
    void getAgents_success() throws Exception {
        AgentVO agent = AgentVO.builder()
                .id("agent-1").name("Test Agent").description("desc")
                .model(AgentDTO.ModelType.DEEPSEEK_CHAT)
                .allowedTools(List.of("tool1")).allowedKbs(List.of()).allowedSkills(List.of())
                .build();

        GetAgentsResponse response = GetAgentsResponse.builder()
                .agents(new AgentVO[]{agent}).build();
        when(agentFacadeService.getAgents()).thenReturn(response);

        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.agents[0].id").value("agent-1"))
                .andExpect(jsonPath("$.data.agents[0].name").value("Test Agent"));
    }

    @Test
    void getAgents_empty() throws Exception {
        GetAgentsResponse response = GetAgentsResponse.builder()
                .agents(new AgentVO[]{}).build();
        when(agentFacadeService.getAgents()).thenReturn(response);

        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.agents").isEmpty());
    }

    @Test
    void getAgents_serviceThrows() throws Exception {
        when(agentFacadeService.getAgents()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void createAgent_success() throws Exception {
        CreateAgentRequest request = new CreateAgentRequest();
        request.setName("New Agent");
        request.setDescription("desc");
        request.setSystemPrompt("You are helpful");
        request.setModel("DEEPSEEK_CHAT");
        request.setAllowedTools(List.of("WebSearchTool"));

        CreateAgentResponse response = CreateAgentResponse.builder()
                .agentId("agent-new").build();
        when(agentFacadeService.createAgent(any())).thenReturn(response);

        mockMvc.perform(post("/api/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.agentId").value("agent-new"));
    }

    @Test
    void createAgent_serviceThrows() throws Exception {
        CreateAgentRequest request = new CreateAgentRequest();
        request.setName("Bad Agent");
        when(agentFacadeService.createAgent(any())).thenThrow(new RuntimeException("Create failed"));

        mockMvc.perform(post("/api/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void deleteAgent_success() throws Exception {
        doNothing().when(agentFacadeService).deleteAgent("agent-1");

        mockMvc.perform(delete("/api/agents/agent-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(agentFacadeService).deleteAgent("agent-1");
    }

    @Test
    void deleteAgent_serviceThrows() throws Exception {
        doThrow(new RuntimeException("Not found")).when(agentFacadeService).deleteAgent("bad-id");

        mockMvc.perform(delete("/api/agents/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void updateAgent_success() throws Exception {
        UpdateAgentRequest request = new UpdateAgentRequest();
        request.setName("Updated Name");
        doNothing().when(agentFacadeService).updateAgent(eq("agent-1"), any());

        mockMvc.perform(patch("/api/agents/agent-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(agentFacadeService).updateAgent(eq("agent-1"), any());
    }

    @Test
    void updateAgent_serviceThrows() throws Exception {
        UpdateAgentRequest request = new UpdateAgentRequest();
        request.setName("Bad Update");
        doThrow(new RuntimeException("Update failed")).when(agentFacadeService).updateAgent(eq("bad-id"), any());

        mockMvc.perform(patch("/api/agents/bad-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
