package com.kama.chatmind.service;

import com.kama.chatmind.model.request.CreateAgentRequest;
import com.kama.chatmind.model.request.UpdateAgentRequest;
import com.kama.chatmind.model.response.CreateAgentResponse;
import com.kama.chatmind.model.response.GetAgentsResponse;

public interface AgentFacadeService {
    GetAgentsResponse getAgents();

    CreateAgentResponse createAgent(CreateAgentRequest request);

    void deleteAgent(String agentId);

    void updateAgent(String agentId, UpdateAgentRequest request);
}
