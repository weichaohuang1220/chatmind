package com.kama.chatmind.service.impl;

import com.kama.chatmind.service.ConfirmationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ConfirmationServiceImpl implements ConfirmationService {

    private final ConcurrentMap<String, CompletableFuture<Boolean>> pendingConfirmations = new ConcurrentHashMap<>();

    private static final Set<String> HIGH_RISK_TOOLS = Set.of(
            "DatabaseQueryTool",
            "EmailTool"
    );

    @Override
    public CompletableFuture<Boolean> requestConfirmation(String confirmationId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.orTimeout(60, TimeUnit.SECONDS)
              .whenComplete((result, ex) -> {
                  pendingConfirmations.remove(confirmationId);
                  if (ex != null) {
                      log.warn("确认请求超时或异常, confirmationId: {}, error: {}", confirmationId, ex.getMessage());
                  }
              });
        pendingConfirmations.put(confirmationId, future);
        log.info("确认请求已创建, confirmationId: {}", confirmationId);
        return future;
    }

    @Override
    public void confirm(String confirmationId, boolean approved) {
        CompletableFuture<Boolean> future = pendingConfirmations.remove(confirmationId);
        if (future != null) {
            future.complete(approved);
            log.info("确认请求已处理, confirmationId: {}, approved: {}", confirmationId, approved);
        } else {
            log.warn("未找到确认请求, confirmationId: {}", confirmationId);
        }
    }

    @Override
    public boolean isHighRiskTool(String toolName) {
        return HIGH_RISK_TOOLS.contains(toolName);
    }
}
