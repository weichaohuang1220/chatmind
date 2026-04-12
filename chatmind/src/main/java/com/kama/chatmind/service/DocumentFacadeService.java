package com.kama.chatmind.service;

import com.kama.chatmind.model.request.CreateDocumentRequest;
import com.kama.chatmind.model.request.UpdateDocumentRequest;
import com.kama.chatmind.model.response.CreateDocumentResponse;
import com.kama.chatmind.model.response.GetDocumentsResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentFacadeService {
    GetDocumentsResponse getDocuments();

    GetDocumentsResponse getDocumentsByKbId(String kbId);

    CreateDocumentResponse createDocument(CreateDocumentRequest request);

    CreateDocumentResponse uploadDocument(String kbId, MultipartFile file);

    void deleteDocument(String documentId);

    void updateDocument(String documentId, UpdateDocumentRequest request);

    void reprocessDocument(String documentId);
}
