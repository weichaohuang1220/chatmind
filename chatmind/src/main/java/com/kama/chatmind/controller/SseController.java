package com.kama.chatmind.controller;

import com.kama.chatmind.model.common.ApiResponse;
import com.kama.chatmind.service.ConfirmationService;
import com.kama.chatmind.service.SseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sse")
@AllArgsConstructor
public class SseController {

    private final SseService sseService;
    private final ConfirmationService confirmationService;

    @RequestMapping(value = "/connect/{chatSessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@PathVariable String chatSessionId) {
        log.info("[SseController] connect called, chatSessionId: {}", chatSessionId);
        return sseService.connect(chatSessionId);
    }

    @PostMapping("/confirm/{confirmationId}")
    public ApiResponse<Void> confirm(@PathVariable String confirmationId,
                                     @RequestBody Map<String, Boolean> body) {
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        log.info("[SseController] confirm called, confirmationId: {}, approved: {}", confirmationId, approved);
        try {
            confirmationService.confirm(confirmationId, approved);
            log.info("[SseController] confirm success, confirmationId: {}", confirmationId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[SseController] confirm failed, confirmationId: {}", confirmationId, e);
            throw e;
        }
    }
}
