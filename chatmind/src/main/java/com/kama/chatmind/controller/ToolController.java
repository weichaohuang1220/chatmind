package com.kama.chatmind.controller;

import com.kama.chatmind.agent.tools.Tool;
import com.kama.chatmind.model.common.ApiResponse;
import com.kama.chatmind.service.ToolFacadeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ToolController {

    private final ToolFacadeService toolFacadeService;

    @GetMapping("/tools")
    public ApiResponse<List<Tool>> getOptionalTools() {
        log.info("[ToolController] getOptionalTools called");
        try {
            List<Tool> tools = toolFacadeService.getOptionalTools();
            log.info("[ToolController] getOptionalTools success, count: {}", tools.size());
            return ApiResponse.success(tools);
        } catch (Exception e) {
            log.error("[ToolController] getOptionalTools failed", e);
            throw e;
        }
    }
}
