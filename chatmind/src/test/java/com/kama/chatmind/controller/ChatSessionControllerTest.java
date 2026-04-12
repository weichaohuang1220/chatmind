package com.kama.chatmind.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.model.request.CreateChatSessionRequest;
import com.kama.chatmind.model.request.UpdateChatSessionRequest;
import com.kama.chatmind.model.response.CreateChatSessionResponse;
import com.kama.chatmind.model.response.GetChatSessionResponse;
import com.kama.chatmind.model.response.GetChatSessionsResponse;
import com.kama.chatmind.model.vo.ChatSessionVO;
import com.kama.chatmind.service.ChatSessionFacadeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatSessionController.class)
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatSessionFacadeService chatSessionFacadeService;

    @Test
    void getChatSessions_success() throws Exception {
        ChatSessionVO session = ChatSessionVO.builder()
                .id("session-1").agentId("agent-1").title("Test Session").build();

        GetChatSessionsResponse response = GetChatSessionsResponse.builder()
                .chatSessions(new ChatSessionVO[]{session}).build();
        when(chatSessionFacadeService.getChatSessions()).thenReturn(response);

        mockMvc.perform(get("/api/chat-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chatSessions[0].id").value("session-1"))
                .andExpect(jsonPath("$.data.chatSessions[0].title").value("Test Session"));
    }

    @Test
    void getChatSessions_serviceThrows() throws Exception {
        when(chatSessionFacadeService.getChatSessions()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/chat-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void getChatSessionById_success() throws Exception {
        ChatSessionVO session = ChatSessionVO.builder()
                .id("session-1").agentId("agent-1").title("My Session").build();

        GetChatSessionResponse response = GetChatSessionResponse.builder()
                .chatSession(session).build();
        when(chatSessionFacadeService.getChatSession("session-1")).thenReturn(response);

        mockMvc.perform(get("/api/chat-sessions/session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chatSession.id").value("session-1"));
    }

    @Test
    void getChatSessionById_serviceThrows() throws Exception {
        when(chatSessionFacadeService.getChatSession("bad-id")).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/chat-sessions/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void getChatSessionsByAgentId_success() throws Exception {
        ChatSessionVO session = ChatSessionVO.builder()
                .id("session-1").agentId("agent-1").title("Session").build();

        GetChatSessionsResponse response = GetChatSessionsResponse.builder()
                .chatSessions(new ChatSessionVO[]{session}).build();
        when(chatSessionFacadeService.getChatSessionsByAgentId("agent-1")).thenReturn(response);

        mockMvc.perform(get("/api/chat-sessions/agent/agent-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chatSessions[0].agentId").value("agent-1"));
    }

    @Test
    void createChatSession_success() throws Exception {
        CreateChatSessionRequest request = new CreateChatSessionRequest();
        request.setAgentId("agent-1");
        request.setTitle("New Session");

        CreateChatSessionResponse response = CreateChatSessionResponse.builder()
                .chatSessionId("session-new").build();
        when(chatSessionFacadeService.createChatSession(any())).thenReturn(response);

        mockMvc.perform(post("/api/chat-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chatSessionId").value("session-new"));
    }

    @Test
    void createChatSession_serviceThrows() throws Exception {
        CreateChatSessionRequest request = new CreateChatSessionRequest();
        request.setAgentId("agent-1");
        when(chatSessionFacadeService.createChatSession(any())).thenThrow(new RuntimeException("Failed"));

        mockMvc.perform(post("/api/chat-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void deleteChatSession_success() throws Exception {
        doNothing().when(chatSessionFacadeService).deleteChatSession("session-1");

        mockMvc.perform(delete("/api/chat-sessions/session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(chatSessionFacadeService).deleteChatSession("session-1");
    }

    @Test
    void deleteChatSession_serviceThrows() throws Exception {
        doThrow(new RuntimeException("Not found")).when(chatSessionFacadeService).deleteChatSession("bad-id");

        mockMvc.perform(delete("/api/chat-sessions/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void updateChatSession_success() throws Exception {
        UpdateChatSessionRequest request = new UpdateChatSessionRequest();
        request.setTitle("Updated Title");
        doNothing().when(chatSessionFacadeService).updateChatSession(eq("session-1"), any());

        mockMvc.perform(patch("/api/chat-sessions/session-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(chatSessionFacadeService).updateChatSession(eq("session-1"), any());
    }

    @Test
    void updateChatSession_serviceThrows() throws Exception {
        UpdateChatSessionRequest request = new UpdateChatSessionRequest();
        request.setTitle("Bad");
        doThrow(new RuntimeException("Failed")).when(chatSessionFacadeService).updateChatSession(eq("bad-id"), any());

        mockMvc.perform(patch("/api/chat-sessions/bad-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
