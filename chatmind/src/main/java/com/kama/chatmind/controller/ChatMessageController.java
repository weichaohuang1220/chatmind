package com.kama.chatmind.controller;

import com.kama.chatmind.model.common.ApiResponse;
import com.kama.chatmind.model.request.CreateChatMessageRequest;
import com.kama.chatmind.model.request.UpdateChatMessageRequest;
import com.kama.chatmind.model.response.CreateChatMessageResponse;
import com.kama.chatmind.model.response.GetChatMessagesResponse;
import com.kama.chatmind.service.ChatMessageFacadeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ChatMessageController {

    private final ChatMessageFacadeService chatMessageFacadeService;

    // 根据 sessionId 查询聊天消息
    @GetMapping("/chat-messages/session/{sessionId}")
    public ApiResponse<GetChatMessagesResponse> getChatMessagesBySessionId(@PathVariable String sessionId) {
        log.info("[ChatMessageController] getChatMessagesBySessionId called, sessionId: {}", sessionId);
        try {
            GetChatMessagesResponse response = chatMessageFacadeService.getChatMessagesBySessionId(sessionId);
            log.info("[ChatMessageController] getChatMessagesBySessionId success, sessionId: {}, count: {}",
                    sessionId, response.getChatMessages() != null ? response.getChatMessages().length : 0);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[ChatMessageController] getChatMessagesBySessionId failed, sessionId: {}", sessionId, e);
            throw e;
        }
    }

    // 创建聊天消息
    @PostMapping("/chat-messages")
    public ApiResponse<CreateChatMessageResponse> createChatMessage(@RequestBody CreateChatMessageRequest request) {
        log.info("[ChatMessageController] createChatMessage called, sessionId: {}, messageLength: {}",
                request.getSessionId(),
                request.getContent() != null ? request.getContent().length() : 0);
        try {
            CreateChatMessageResponse response = chatMessageFacadeService.createChatMessage(request);
            log.info("[ChatMessageController] createChatMessage success, chatMessageId: {}", response.getChatMessageId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[ChatMessageController] createChatMessage failed", e);
            throw e;
        }
    }

    // 删除聊天消息
    @DeleteMapping("/chat-messages/{chatMessageId}")
    public ApiResponse<Void> deleteChatMessage(@PathVariable String chatMessageId) {
        log.info("[ChatMessageController] deleteChatMessage called, chatMessageId: {}", chatMessageId);
        try {
            chatMessageFacadeService.deleteChatMessage(chatMessageId);
            log.info("[ChatMessageController] deleteChatMessage success, chatMessageId: {}", chatMessageId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[ChatMessageController] deleteChatMessage failed, chatMessageId: {}", chatMessageId, e);
            throw e;
        }
    }

    // 更新聊天消息
    @PatchMapping("/chat-messages/{chatMessageId}")
    public ApiResponse<Void> updateChatMessage(@PathVariable String chatMessageId, @RequestBody UpdateChatMessageRequest request) {
        log.info("[ChatMessageController] updateChatMessage called, chatMessageId: {}", chatMessageId);
        try {
            chatMessageFacadeService.updateChatMessage(chatMessageId, request);
            log.info("[ChatMessageController] updateChatMessage success, chatMessageId: {}", chatMessageId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[ChatMessageController] updateChatMessage failed, chatMessageId: {}", chatMessageId, e);
            throw e;
        }
    }
}
