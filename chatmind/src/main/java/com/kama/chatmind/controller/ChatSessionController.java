package com.kama.chatmind.controller;

import com.kama.chatmind.model.common.ApiResponse;
import com.kama.chatmind.model.request.CreateChatSessionRequest;
import com.kama.chatmind.model.request.UpdateChatSessionRequest;
import com.kama.chatmind.model.response.CreateChatSessionResponse;
import com.kama.chatmind.model.response.GetChatSessionResponse;
import com.kama.chatmind.model.response.GetChatSessionsResponse;
import com.kama.chatmind.service.ChatSessionFacadeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ChatSessionController {

    private final ChatSessionFacadeService chatSessionFacadeService;

    // 查询所有聊天会话
    @GetMapping("/chat-sessions")
    public ApiResponse<GetChatSessionsResponse> getChatSessions() {
        log.info("[ChatSessionController] getChatSessions called");
        try {
            GetChatSessionsResponse response = chatSessionFacadeService.getChatSessions();
            log.info("[ChatSessionController] getChatSessions success, count: {}",
                    response.getChatSessions() != null ? response.getChatSessions().length : 0);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[ChatSessionController] getChatSessions failed", e);
            throw e;
        }
    }

    // 查询单个聊天会话
    @GetMapping("/chat-sessions/{chatSessionId}")
    public ApiResponse<GetChatSessionResponse> getChatSession(@PathVariable String chatSessionId) {
        log.info("[ChatSessionController] getChatSession called, chatSessionId: {}", chatSessionId);
        try {
            GetChatSessionResponse response = chatSessionFacadeService.getChatSession(chatSessionId);
            log.info("[ChatSessionController] getChatSession success, chatSessionId: {}", chatSessionId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[ChatSessionController] getChatSession failed, chatSessionId: {}", chatSessionId, e);
            throw e;
        }
    }

    // 根据 agentId 查询聊天会话
    @GetMapping("/chat-sessions/agent/{agentId}")
    public ApiResponse<GetChatSessionsResponse> getChatSessionsByAgentId(@PathVariable String agentId) {
        log.info("[ChatSessionController] getChatSessionsByAgentId called, agentId: {}", agentId);
        try {
            GetChatSessionsResponse response = chatSessionFacadeService.getChatSessionsByAgentId(agentId);
            log.info("[ChatSessionController] getChatSessionsByAgentId success, agentId: {}, count: {}",
                    agentId, response.getChatSessions() != null ? response.getChatSessions().length : 0);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[ChatSessionController] getChatSessionsByAgentId failed, agentId: {}", agentId, e);
            throw e;
        }
    }

    // 创建聊天会话
    @PostMapping("/chat-sessions")
    public ApiResponse<CreateChatSessionResponse> createChatSession(@RequestBody CreateChatSessionRequest request) {
        log.info("[ChatSessionController] createChatSession called, agentId: {}", request.getAgentId());
        try {
            CreateChatSessionResponse response = chatSessionFacadeService.createChatSession(request);
            log.info("[ChatSessionController] createChatSession success, chatSessionId: {}", response.getChatSessionId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[ChatSessionController] createChatSession failed", e);
            throw e;
        }
    }

    // 删除聊天会话
    @DeleteMapping("/chat-sessions/{chatSessionId}")
    public ApiResponse<Void> deleteChatSession(@PathVariable String chatSessionId) {
        log.info("[ChatSessionController] deleteChatSession called, chatSessionId: {}", chatSessionId);
        try {
            chatSessionFacadeService.deleteChatSession(chatSessionId);
            log.info("[ChatSessionController] deleteChatSession success, chatSessionId: {}", chatSessionId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[ChatSessionController] deleteChatSession failed, chatSessionId: {}", chatSessionId, e);
            throw e;
        }
    }

    // 更新聊天会话
    @PatchMapping("/chat-sessions/{chatSessionId}")
    public ApiResponse<Void> updateChatSession(@PathVariable String chatSessionId, @RequestBody UpdateChatSessionRequest request) {
        log.info("[ChatSessionController] updateChatSession called, chatSessionId: {}", chatSessionId);
        try {
            chatSessionFacadeService.updateChatSession(chatSessionId, request);
            log.info("[ChatSessionController] updateChatSession success, chatSessionId: {}", chatSessionId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[ChatSessionController] updateChatSession failed, chatSessionId: {}", chatSessionId, e);
            throw e;
        }
    }
}
