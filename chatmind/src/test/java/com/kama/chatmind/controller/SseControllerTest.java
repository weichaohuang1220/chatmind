package com.kama.chatmind.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.service.ConfirmationService;
import com.kama.chatmind.service.SseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SseController.class)
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private ConfirmationService confirmationService;

    @Test
    void connect_success() throws Exception {
        SseEmitter emitter = new SseEmitter();
        when(sseService.connect("session-1")).thenReturn(emitter);

        mockMvc.perform(get("/sse/connect/session-1")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());

        verify(sseService).connect("session-1");
    }

    @Test
    void connect_serviceThrows() {
        when(sseService.connect("bad-id")).thenThrow(new RuntimeException("Connection failed"));

        assertThrows(Exception.class, () ->
                mockMvc.perform(get("/sse/connect/bad-id")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE)));
    }

    @Test
    void confirm_approved() throws Exception {
        doNothing().when(confirmationService).confirm("confirm-1", true);

        mockMvc.perform(post("/sse/confirm/confirm-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(confirmationService).confirm("confirm-1", true);
    }

    @Test
    void confirm_rejected() throws Exception {
        doNothing().when(confirmationService).confirm("confirm-2", false);

        mockMvc.perform(post("/sse/confirm/confirm-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(confirmationService).confirm("confirm-2", false);
    }

    @Test
    void confirm_serviceThrows() throws Exception {
        doThrow(new RuntimeException("Invalid confirmation"))
                .when(confirmationService).confirm("bad-id", true);

        mockMvc.perform(post("/sse/confirm/bad-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
