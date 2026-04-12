package com.kama.chatmind.service;

import com.kama.chatmind.model.request.CreateChatSessionRequest;
import com.kama.chatmind.model.request.UpdateChatSessionRequest;
import com.kama.chatmind.model.response.CreateChatSessionResponse;
import com.kama.chatmind.model.response.GetChatSessionResponse;
import com.kama.chatmind.model.response.GetChatSessionsResponse;

public interface ChatSessionFacadeService {
    GetChatSessionsResponse getChatSessions();

    GetChatSessionResponse getChatSession(String chatSessionId);

    GetChatSessionsResponse getChatSessionsByAgentId(String agentId);

    CreateChatSessionResponse createChatSession(CreateChatSessionRequest request);

    void deleteChatSession(String chatSessionId);

    void updateChatSession(String chatSessionId, UpdateChatSessionRequest request);
}
