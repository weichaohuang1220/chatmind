package com.kama.chatmind.service;

import com.kama.chatmind.model.dto.ChatMessageDTO;
import com.kama.chatmind.model.request.CreateChatMessageRequest;
import com.kama.chatmind.model.request.UpdateChatMessageRequest;
import com.kama.chatmind.model.response.CreateChatMessageResponse;
import com.kama.chatmind.model.response.GetChatMessagesResponse;

import java.util.List;

public interface ChatMessageFacadeService {
    GetChatMessagesResponse getChatMessagesBySessionId(String sessionId);

    List<ChatMessageDTO> getChatMessagesBySessionIdRecently(String sessionId, int limit);

    CreateChatMessageResponse createChatMessage(CreateChatMessageRequest request);

    CreateChatMessageResponse createChatMessage(ChatMessageDTO chatMessageDTO);

    CreateChatMessageResponse agentCreateChatMessage(CreateChatMessageRequest request);

    CreateChatMessageResponse appendChatMessage(String chatMessageId, String appendContent);

    void deleteChatMessage(String chatMessageId);

    void updateChatMessage(String chatMessageId, UpdateChatMessageRequest request);
}
