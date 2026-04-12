package com.kama.chatmind.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.chatmind.message.SseMessage;
import com.kama.chatmind.service.SseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@AllArgsConstructor
public class SseServiceImpl implements SseService {

    private final ConcurrentMap<String, SseEmitter> clients = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public SseEmitter connect(String chatSessionId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        clients.put(chatSessionId, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("connected")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        emitter.onCompletion(() -> {
            clients.remove(chatSessionId);
        });
        emitter.onTimeout(() -> clients.remove(chatSessionId));
        emitter.onError((error) -> clients.remove(chatSessionId));

        return emitter;
    }

    @Override
    public void send(String chatSessionId, SseMessage message) {
        SseEmitter emitter = clients.get(chatSessionId);

        if (emitter != null) {
            try {
                // 将消息转换为字符串
                String sseMessageStr = objectMapper.writeValueAsString(message);
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(sseMessageStr)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("No SSE client found for chatSessionId: {}, message dropped", chatSessionId);
        }
    }

    @Override
    public void sendToken(String chatSessionId, String token) {
        SseEmitter emitter = clients.get(chatSessionId);
        if (emitter != null) {
            try {
                SseMessage message = SseMessage.builder()
                        .type(SseMessage.Type.AI_TOKEN)
                        .payload(SseMessage.Payload.builder()
                                .token(token)
                                .build())
                        .build();
                String sseMessageStr = objectMapper.writeValueAsString(message);
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(sseMessageStr)
                );
            } catch (IOException e) {
                log.warn("Failed to send token to chatSessionId: {}", chatSessionId, e);
            }
        } else {
            log.warn("No SSE client found for chatSessionId: {}, token dropped", chatSessionId);
        }
    }
}
