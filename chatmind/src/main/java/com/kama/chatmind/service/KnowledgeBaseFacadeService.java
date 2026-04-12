package com.kama.chatmind.service;

import com.kama.chatmind.model.request.CreateKnowledgeBaseRequest;
import com.kama.chatmind.model.request.UpdateKnowledgeBaseRequest;
import com.kama.chatmind.model.response.CreateKnowledgeBaseResponse;
import com.kama.chatmind.model.response.GetKnowledgeBasesResponse;

public interface KnowledgeBaseFacadeService {
    GetKnowledgeBasesResponse getKnowledgeBases();

    CreateKnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request);

    void deleteKnowledgeBase(String knowledgeBaseId);

    void updateKnowledgeBase(String knowledgeBaseId, UpdateKnowledgeBaseRequest request);
}

