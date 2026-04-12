package com.kama.chatmind.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.model.request.CreateKnowledgeBaseRequest;
import com.kama.chatmind.model.request.UpdateKnowledgeBaseRequest;
import com.kama.chatmind.model.response.CreateKnowledgeBaseResponse;
import com.kama.chatmind.model.response.GetKnowledgeBasesResponse;
import com.kama.chatmind.model.vo.KnowledgeBaseVO;
import com.kama.chatmind.service.KnowledgeBaseFacadeService;
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

@WebMvcTest(KnowledgeBaseController.class)
class KnowledgeBaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeBaseFacadeService knowledgeBaseFacadeService;

    @Test
    void getKnowledgeBases_success() throws Exception {
        KnowledgeBaseVO kb = KnowledgeBaseVO.builder()
                .id("kb-1").name("Test KB").description("A test knowledge base").build();

        GetKnowledgeBasesResponse response = GetKnowledgeBasesResponse.builder()
                .knowledgeBases(new KnowledgeBaseVO[]{kb}).build();
        when(knowledgeBaseFacadeService.getKnowledgeBases()).thenReturn(response);

        mockMvc.perform(get("/api/knowledge-bases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.knowledgeBases[0].id").value("kb-1"))
                .andExpect(jsonPath("$.data.knowledgeBases[0].name").value("Test KB"));
    }

    @Test
    void getKnowledgeBases_empty() throws Exception {
        GetKnowledgeBasesResponse response = GetKnowledgeBasesResponse.builder()
                .knowledgeBases(new KnowledgeBaseVO[]{}).build();
        when(knowledgeBaseFacadeService.getKnowledgeBases()).thenReturn(response);

        mockMvc.perform(get("/api/knowledge-bases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeBases").isEmpty());
    }

    @Test
    void getKnowledgeBases_serviceThrows() throws Exception {
        when(knowledgeBaseFacadeService.getKnowledgeBases()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/knowledge-bases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void createKnowledgeBase_success() throws Exception {
        CreateKnowledgeBaseRequest request = new CreateKnowledgeBaseRequest();
        request.setName("New KB");
        request.setDescription("desc");

        CreateKnowledgeBaseResponse response = CreateKnowledgeBaseResponse.builder()
                .knowledgeBaseId("kb-new").build();
        when(knowledgeBaseFacadeService.createKnowledgeBase(any())).thenReturn(response);

        mockMvc.perform(post("/api/knowledge-bases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.knowledgeBaseId").value("kb-new"));
    }

    @Test
    void createKnowledgeBase_serviceThrows() throws Exception {
        CreateKnowledgeBaseRequest request = new CreateKnowledgeBaseRequest();
        request.setName("Bad KB");
        when(knowledgeBaseFacadeService.createKnowledgeBase(any())).thenThrow(new RuntimeException("Failed"));

        mockMvc.perform(post("/api/knowledge-bases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void deleteKnowledgeBase_success() throws Exception {
        doNothing().when(knowledgeBaseFacadeService).deleteKnowledgeBase("kb-1");

        mockMvc.perform(delete("/api/knowledge-bases/kb-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(knowledgeBaseFacadeService).deleteKnowledgeBase("kb-1");
    }

    @Test
    void deleteKnowledgeBase_serviceThrows() throws Exception {
        doThrow(new RuntimeException("Not found")).when(knowledgeBaseFacadeService).deleteKnowledgeBase("bad-id");

        mockMvc.perform(delete("/api/knowledge-bases/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void updateKnowledgeBase_success() throws Exception {
        UpdateKnowledgeBaseRequest request = new UpdateKnowledgeBaseRequest();
        request.setName("Updated KB");
        request.setDescription("updated desc");
        doNothing().when(knowledgeBaseFacadeService).updateKnowledgeBase(eq("kb-1"), any());

        mockMvc.perform(patch("/api/knowledge-bases/kb-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(knowledgeBaseFacadeService).updateKnowledgeBase(eq("kb-1"), any());
    }

    @Test
    void updateKnowledgeBase_serviceThrows() throws Exception {
        UpdateKnowledgeBaseRequest request = new UpdateKnowledgeBaseRequest();
        request.setName("Bad");
        doThrow(new RuntimeException("Failed")).when(knowledgeBaseFacadeService).updateKnowledgeBase(eq("bad-id"), any());

        mockMvc.perform(patch("/api/knowledge-bases/bad-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
