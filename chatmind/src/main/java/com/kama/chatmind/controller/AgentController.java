package com.kama.chatmind.controller;

import com.kama.chatmind.model.common.ApiResponse;
import com.kama.chatmind.model.request.CreateAgentRequest;
import com.kama.chatmind.model.request.UpdateAgentRequest;
import com.kama.chatmind.model.response.CreateAgentResponse;
import com.kama.chatmind.model.response.GetAgentsResponse;
import com.kama.chatmind.service.AgentFacadeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AgentController {

    private final AgentFacadeService agentFacadeService;

    // 查询 agents
    @GetMapping("/agents")
    public ApiResponse<GetAgentsResponse> getAgents() {
        log.info("[AgentController] getAgents called");
        try {
            GetAgentsResponse response = agentFacadeService.getAgents();
            log.info("[AgentController] getAgents success, count: {}", response.getAgents() != null ? response.getAgents().length : 0);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[AgentController] getAgents failed", e);
            throw e;
        }
    }

    // 创建 agent
    @PostMapping("/agents")
    public ApiResponse<CreateAgentResponse> createAgent(@RequestBody CreateAgentRequest request) {
        log.info("[AgentController] createAgent called, request: {}", request);
        try {
            CreateAgentResponse response = agentFacadeService.createAgent(request);
            log.info("[AgentController] createAgent success, agentId: {}", response.getAgentId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("[AgentController] createAgent failed", e);
            throw e;
        }
    }

    // 删除 agent
    @DeleteMapping("/agents/{agentId}")
    public ApiResponse<Void> deleteAgent(@PathVariable String agentId) {
        log.info("[AgentController] deleteAgent called, agentId: {}", agentId);
        try {
            agentFacadeService.deleteAgent(agentId);
            log.info("[AgentController] deleteAgent success, agentId: {}", agentId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[AgentController] deleteAgent failed, agentId: {}", agentId, e);
            throw e;
        }
    }

    // 更新 agent
    @PatchMapping("/agents/{agentId}")
    public ApiResponse<Void> updateAgent(@PathVariable String agentId, @RequestBody UpdateAgentRequest request) {
        log.info("[AgentController] updateAgent called, agentId: {}", agentId);
        try {
            agentFacadeService.updateAgent(agentId, request);
            log.info("[AgentController] updateAgent success, agentId: {}", agentId);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("[AgentController] updateAgent failed, agentId: {}", agentId, e);
            throw e;
        }
    }
}
