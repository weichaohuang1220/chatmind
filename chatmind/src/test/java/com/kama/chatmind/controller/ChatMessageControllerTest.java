package com.kama.chatmind.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.model.dto.ChatMessageDTO;
import com.kama.chatmind.model.request.CreateChatMessageRequest;
import com.kama.chatmind.model.request.UpdateChatMessageRequest;
import com.kama.chatmind.model.response.CreateChatMessageResponse;
import com.kama.chatmind.model.response.GetChatMessagesResponse;
import com.kama.chatmind.model.vo.ChatMessageVO;
import com.kama.chatmind.service.ChatMessageFacadeService;
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

@WebMvcTest(ChatMessageController.class)
class ChatMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatMessageFacadeService chatMessageFacadeService;

    @Test
    void getChatMessagesBySessionId_success() throws Exception {
        ChatMessageVO msg = ChatMessageVO.builder()
                .id("msg-1").sessionId("session-1")
                .role(ChatMessageDTO.RoleType.USER).content("Hello")
                .build();

        GetChatMessagesResponse response = GetChatMessagesResponse.builder()
                .chatMessages(new ChatMessageVO[]{msg}).build();
        when(chatMessageFacadeService.getChatMessagesBySessionId("session-1")).thenReturn(response);

        mockMvc.perform(get("/api/chat-messages/session/session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chatMessages[0].id").value("msg-1"))
                .andExpect(jsonPath("$.data.chatMessages[0].content").value("Hello"));
    }

    @Test
    void getChatMessagesBySessionId_empty() throws Exception {
        GetChatMessagesResponse response = GetChatMessagesResponse.builder()
                .chatMessages(new ChatMessageVO[]{}).build();
        when(chatMessageFacadeService.getChatMessagesBySessionId("session-1")).thenReturn(response);

        mockMvc.perform(get("/api/chat-messages/session/session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chatMessages").isEmpty());
    }

    @Test
    void getChatMessagesBySessionId_serviceThrows() throws Exception {
        when(chatMessageFacadeService.getChatMessagesBySessionId("bad-id"))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/chat-messages/session/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void createChatMessage_success() throws Exception {
        CreateChatMessageRequest request = CreateChatMessageRequest.builder()
                .agentId("agent-1").sessionId("session-1")
                .role(ChatMessageDTO.RoleType.USER).content("Hello AI")
                .build();

        CreateChatMessageResponse response = CreateChatMessageResponse.builder()
                .chatMessageId("msg-new").build();
        when(chatMessageFacadeService.createChatMessage(any(CreateChatMessageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/chat-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chatMessageId").value("msg-new"));
    }

    @Test
    void createChatMessage_serviceThrows() throws Exception {
        CreateChatMessageRequest request = CreateChatMessageRequest.builder()
                .content("fail").build();
        when(chatMessageFacadeService.createChatMessage(any(CreateChatMessageRequest.class)))
                .thenThrow(new RuntimeException("Create failed"));

        mockMvc.perform(post("/api/chat-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void deleteChatMessage_success() throws Exception {
        doNothing().when(chatMessageFacadeService).deleteChatMessage("msg-1");

        mockMvc.perform(delete("/api/chat-messages/msg-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(chatMessageFacadeService).deleteChatMessage("msg-1");
    }

    @Test
    void deleteChatMessage_serviceThrows() throws Exception {
        doThrow(new RuntimeException("Not found")).when(chatMessageFacadeService).deleteChatMessage("bad-id");

        mockMvc.perform(delete("/api/chat-messages/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void updateChatMessage_success() throws Exception {
        UpdateChatMessageRequest request = new UpdateChatMessageRequest();
        request.setContent("Updated content");
        doNothing().when(chatMessageFacadeService).updateChatMessage(eq("msg-1"), any());

        mockMvc.perform(patch("/api/chat-messages/msg-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(chatMessageFacadeService).updateChatMessage(eq("msg-1"), any());
    }

    @Test
    void updateChatMessage_serviceThrows() throws Exception {
        UpdateChatMessageRequest request = new UpdateChatMessageRequest();
        request.setContent("Bad");
        doThrow(new RuntimeException("Failed")).when(chatMessageFacadeService).updateChatMessage(eq("bad-id"), any());

        mockMvc.perform(patch("/api/chat-messages/bad-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
