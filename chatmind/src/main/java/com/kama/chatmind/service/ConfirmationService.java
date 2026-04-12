package com.kama.chatmind.service;

import java.util.concurrent.CompletableFuture;

public interface ConfirmationService {

    /**
     * 创建一个确认请求，返回一个 CompletableFuture 用于等待用户确认结果
     */
    CompletableFuture<Boolean> requestConfirmation(String confirmationId);

    /**
     * 用户确认或拒绝某个确认请求
     */
    void confirm(String confirmationId, boolean approved);

    /**
     * 判断某个工具是否为高风险工具，需要用户确认
     */
    boolean isHighRiskTool(String toolName);
}
