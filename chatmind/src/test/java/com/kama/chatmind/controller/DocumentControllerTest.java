package com.kama.chatmind.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.model.request.CreateDocumentRequest;
import com.kama.chatmind.model.request.UpdateDocumentRequest;
import com.kama.chatmind.model.response.CreateDocumentResponse;
import com.kama.chatmind.model.response.GetDocumentsResponse;
import com.kama.chatmind.model.vo.DocumentVO;
import com.kama.chatmind.service.DocumentFacadeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentFacadeService documentFacadeService;

    @Test
    void getDocuments_success() throws Exception {
        DocumentVO doc = DocumentVO.builder()
                .id("doc-1").kbId("kb-1").filename("test.md")
                .filetype("markdown").size(1024L).build();

        GetDocumentsResponse response = GetDocumentsResponse.builder()
                .documents(new DocumentVO[]{doc}).build();
        when(documentFacadeService.getDocuments()).thenReturn(response);

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.documents[0].id").value("doc-1"))
                .andExpect(jsonPath("$.data.documents[0].filename").value("test.md"));
    }

    @Test
    void getDocuments_serviceThrows() throws Exception {
        when(documentFacadeService.getDocuments()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void getDocumentsByKbId_success() throws Exception {
        DocumentVO doc = DocumentVO.builder()
                .id("doc-1").kbId("kb-1").filename("test.md")
                .filetype("markdown").size(1024L).build();

        GetDocumentsResponse response = GetDocumentsResponse.builder()
                .documents(new DocumentVO[]{doc}).build();
        when(documentFacadeService.getDocumentsByKbId("kb-1")).thenReturn(response);

        mockMvc.perform(get("/api/documents/kb/kb-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.documents[0].kbId").value("kb-1"));
    }

    @Test
    void getDocumentsByKbId_serviceThrows() throws Exception {
        when(documentFacadeService.getDocumentsByKbId("bad-id")).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/documents/kb/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void createDocument_success() throws Exception {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setKbId("kb-1");
        request.setFilename("new.md");
        request.setFiletype("markdown");
        request.setSize(2048L);

        CreateDocumentResponse response = CreateDocumentResponse.builder()
                .documentId("doc-new").build();
        when(documentFacadeService.createDocument(any())).thenReturn(response);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.documentId").value("doc-new"));
    }

    @Test
    void createDocument_serviceThrows() throws Exception {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setKbId("kb-1");
        when(documentFacadeService.createDocument(any())).thenThrow(new RuntimeException("Failed"));

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void uploadDocument_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.md", "text/markdown", "# Hello\nWorld".getBytes());

        CreateDocumentResponse response = CreateDocumentResponse.builder()
                .documentId("doc-uploaded").build();
        when(documentFacadeService.uploadDocument(eq("kb-1"), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("kbId", "kb-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.documentId").value("doc-uploaded"));
    }

    @Test
    void uploadDocument_serviceThrows() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.md", "text/markdown", "content".getBytes());
        when(documentFacadeService.uploadDocument(eq("kb-1"), any())).thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("kbId", "kb-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void reprocessDocument_success() throws Exception {
        doNothing().when(documentFacadeService).reprocessDocument("doc-1");

        mockMvc.perform(post("/api/documents/doc-1/reprocess"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(documentFacadeService).reprocessDocument("doc-1");
    }

    @Test
    void reprocessDocument_serviceThrows() throws Exception {
        doThrow(new RuntimeException("Reprocess failed")).when(documentFacadeService).reprocessDocument("bad-id");

        mockMvc.perform(post("/api/documents/bad-id/reprocess"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void deleteDocument_success() throws Exception {
        doNothing().when(documentFacadeService).deleteDocument("doc-1");

        mockMvc.perform(delete("/api/documents/doc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(documentFacadeService).deleteDocument("doc-1");
    }

    @Test
    void deleteDocument_serviceThrows() throws Exception {
        doThrow(new RuntimeException("Not found")).when(documentFacadeService).deleteDocument("bad-id");

        mockMvc.perform(delete("/api/documents/bad-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void updateDocument_success() throws Exception {
        UpdateDocumentRequest request = new UpdateDocumentRequest();
        request.setFilename("updated.md");
        doNothing().when(documentFacadeService).updateDocument(eq("doc-1"), any());

        mockMvc.perform(patch("/api/documents/doc-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(documentFacadeService).updateDocument(eq("doc-1"), any());
    }

    @Test
    void updateDocument_serviceThrows() throws Exception {
        UpdateDocumentRequest request = new UpdateDocumentRequest();
        request.setFilename("bad");
        doThrow(new RuntimeException("Failed")).when(documentFacadeService).updateDocument(eq("bad-id"), any());

        mockMvc.perform(patch("/api/documents/bad-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
