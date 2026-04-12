package com.kama.chatmind.agent;

import com.kama.chatmind.converter.ChatMessageConverter;
import com.kama.chatmind.message.SseMessage;
import com.kama.chatmind.model.dto.ChatMessageDTO;
import com.kama.chatmind.model.dto.KnowledgeBaseDTO;
import com.kama.chatmind.model.response.CreateChatMessageResponse;
import com.kama.chatmind.model.vo.ChatMessageVO;
import com.kama.chatmind.service.ChatMessageFacadeService;
import com.kama.chatmind.service.ConfirmationService;
import com.kama.chatmind.service.LongTermMemoryService;
import com.kama.chatmind.service.MemoryCompressionService;
import com.kama.chatmind.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ChatMind {
    // 智能体 ID
    private String agentId;

    // 名称
    private String name;

    // 描述
    private String description;

    // 默认系统提示词
    private String systemPrompt;

    // 交互实例
    private ChatClient chatClient;

    // 状态
    private AgentState agentState;

    // 可用的工具
    private List<ToolCallback> availableTools;

    // 可访问的知识库
    private List<KnowledgeBaseDTO> availableKbs;

    // 工具调用管理器
    private ToolCallingManager toolCallingManager;

    // 模型的聊天记录
    private ChatMemory chatMemory;

    // 模型的聊天会话 ID
    private String chatSessionId;

    // 记忆压缩服务（可选，为 null 时不进行压缩）
    private MemoryCompressionService memoryCompressionService;

    // 长期记忆服务（可选，为 null 时不进行长期记忆的读取和保存）
    private LongTermMemoryService longTermMemoryService;

    // 最多循环次数
    private static final Integer MAX_STEPS = 20;

    private static final Integer DEFAULT_MAX_MESSAGES = 20;

    // 触发记忆压缩的消息数阈值
    private static final Integer COMPRESSION_THRESHOLD = 15;

    // 压缩时保留最近的消息数
    private static final Integer RECENT_MESSAGES_TO_KEEP = 5;

    // SpringAI 自带的 ChatOptions, 不是 AgentDTO.ChatOptions
    private ChatOptions chatOptions;

    // SSE 服务, 用于发送消息给前端
    private SseService sseService;

    // HITL 确认服务
    private ConfirmationService confirmationService;

    private ChatMessageConverter chatMessageConverter;

    private ChatMessageFacadeService chatMessageFacadeService;

    // 最后一次的 ChatResponse
    private ChatResponse lastChatResponse;

    // AI 返回的，已经持久化，但是需要 sse 发给前端的消息
    private final List<ChatMessageDTO> pendingChatMessages = new ArrayList<>();

    // 规划阶段生成的执行计划文本（可选，为 null 时表示未规划或规划失败）
    private String planText;

    public ChatMind() {
    }

    public ChatMind(String agentId,
                     String name,
                     String description,
                     String systemPrompt,
                     ChatClient chatClient,
                     Integer maxMessages,
                     List<Message> memory,
                     List<ToolCallback> availableTools,
                     List<KnowledgeBaseDTO> availableKbs,
                     String chatSessionId,
                     SseService sseService,
                     ChatMessageFacadeService chatMessageFacadeService,
                     ChatMessageConverter chatMessageConverter,
                     MemoryCompressionService memoryCompressionService,
                     ConfirmationService confirmationService,
                     LongTermMemoryService longTermMemoryService
    ) {
        this.agentId = agentId;
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;

        this.chatClient = chatClient;

        this.availableTools = availableTools;
        this.availableKbs = availableKbs;

        this.chatSessionId = chatSessionId;
        this.sseService = sseService;

        this.chatMessageFacadeService = chatMessageFacadeService;
        this.chatMessageConverter = chatMessageConverter;
        this.memoryCompressionService = memoryCompressionService;
        this.confirmationService = confirmationService;
        this.longTermMemoryService = longTermMemoryService;

        this.agentState = AgentState.IDLE;

        // 保存聊天记录
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(maxMessages == null ? DEFAULT_MAX_MESSAGES : maxMessages)
                .build();
        this.chatMemory.add(chatSessionId, memory);

        // 添加系统提示
        if (StringUtils.hasLength(systemPrompt)) {
            this.chatMemory.add(chatSessionId, new SystemMessage(systemPrompt));
        }

        // 关闭 SpringAI 自带的内部的工具调用自动执行功能
        this.chatOptions = DefaultToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();

        // 工具调用管理器
        this.toolCallingManager = ToolCallingManager.builder().build();
    }

    // 打印工具调用信息
    private void logToolCalls(List<AssistantMessage.ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            log.info("\n\n[ToolCalling] 无工具调用");
            return;
        }
        String logMessage = IntStream.range(0, toolCalls.size())
                .mapToObj(i -> {
                    AssistantMessage.ToolCall call = toolCalls.get(i);
                    return String.format(
                            "[ToolCalling #%d]\n- name      : %s\n- arguments : %s",
                            i + 1,
                            call.name(),
                            call.arguments()
                    );
                })
                .collect(Collectors.joining("\n\n"));
        log.info("\n\n========== Tool Calling ==========\n{}\n=================================\n", logMessage);
    }

    // 持久化 Message, 返回 chatMessageId
    // 需要 Agent 持久化的 Message 子类有以下两类
    // AssistantMessage
    // ToolResponseMessage

    // SystemMessage 不需要持久化
    // UserMessage 在每次用户发送问题之间就已经持久化过了
    private void saveMessage(Message message) {
        ChatMessageDTO.ChatMessageDTOBuilder builder = ChatMessageDTO.builder();
        if (message instanceof AssistantMessage assistantMessage) {
            ChatMessageDTO chatMessageDTO = builder.role(ChatMessageDTO.RoleType.ASSISTANT)
                    .content(assistantMessage.getText())
                    .sessionId(this.chatSessionId)
                    .metadata(ChatMessageDTO.MetaData.builder()
                            .toolCalls(assistantMessage.getToolCalls())
                            .build())
                    .build();
            CreateChatMessageResponse chatMessage = chatMessageFacadeService.createChatMessage(chatMessageDTO);
            chatMessageDTO.setId(chatMessage.getChatMessageId());
            pendingChatMessages.add(chatMessageDTO);
        } else if (message instanceof ToolResponseMessage toolResponseMessage) {
            // 持久化 ToolResponseMessage
            for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
                ChatMessageDTO chatMessageDTO = builder.role(ChatMessageDTO.RoleType.TOOL)
                        .content(toolResponse.responseData())
                        .sessionId(this.chatSessionId)
                        .metadata(ChatMessageDTO.MetaData.builder()
                                .toolResponse(toolResponse)
                                .build())
                        .build();
                CreateChatMessageResponse chatMessage = chatMessageFacadeService.createChatMessage(chatMessageDTO);
                chatMessageDTO.setId(chatMessage.getChatMessageId());
                pendingChatMessages.add(chatMessageDTO);
            }
        } else {
            throw new IllegalArgumentException("不支持的 Message 类型: " + message.getClass().getName());
        }
    }

    // 刷新 pendingMessages, 将数据通过 sse 发送给前端
    private void refreshPendingMessages() {
        for (ChatMessageDTO message : pendingChatMessages) {
            ChatMessageVO vo = chatMessageConverter.toVO(message);
            SseMessage sseMessage = SseMessage.builder()
                    .type(SseMessage.Type.AI_GENERATED_CONTENT)
                    .payload(SseMessage.Payload.builder()
                            .message(vo)
                            .build())
                    .metadata(SseMessage.Metadata.builder()
                            .chatMessageId(message.getId())
                            .build())
                    .build();
            sseService.send(this.chatSessionId, sseMessage);
        }
        pendingChatMessages.clear();
    }

    /*
    **四者关系总结**
```
chatMemory（持久对话状态）
    │
    ├──读取──→ think() 发给模型
    │                │
    │                └──→ lastChatResponse（模型单次返回，含 AssistantMessage + toolCalls）
    │                           │
    │                           └──→ execute() 传给 toolCallingManager
    │                                       │
    │                                       └──→ conversationHistory
    │                                             = chatMemory 旧内容
    │                                             + AssistantMessage
    │                                             + ToolResponseMessage
    │
    └──被替换──← conversationHistory 写回
    * */
    // thinkPrompt 应该放到 system 中还是
    private boolean think() {
        //1. 构造决策提示词，告诉模型："你有这些知识库可以用，信息不够就去搜。" 这就是 Agent 的"思考指令"。
        String thinkPrompt = """
                现在你是一个智能的的具体「决策模块」
                请根据当前对话上下文，决定下一步的动作。
                                \s
                【额外信息】
                - 你目前拥有的知识库列表以及描述：%s
                - 如果有缺失的上下文时，优先从知识库中进行搜索
                """.formatted(this.availableKbs);

        // 如果有执行计划，将其注入到思考提示词中
        if (StringUtils.hasText(this.planText)) {
            thinkPrompt += "\n【执行计划】\n" + this.planText + "\n请参考以上计划来决定下一步动作。";
        }

        // 将 thinkPrompt 通过 .user(thinkPrompt) 的方式构造进入 chatClient 中
        // 既能让每次 messageList 的最后一条是 本条提示词，
        // 又能够避免将 thinkPrompt 加入到聊天记录中


        //2.拿出当前会话的所有历史消息，包括system prompt, user, assistant

        Prompt prompt = Prompt.builder()
                .chatOptions(this.chatOptions)
                .messages(this.chatMemory.get(this.chatSessionId))
                .build();
        //3.流式调用：逐 token 发给前端，最后一块保留完整 tool calls
        StringBuilder contentBuilder = new StringBuilder();
        AtomicReference<ChatResponse> lastChunkRef = new AtomicReference<>();

        this.chatClient
                .prompt(prompt)
                .system(thinkPrompt)
                .toolCallbacks(this.availableTools.toArray(new ToolCallback[0]))
                .stream()
                .chatResponse()
                .doOnNext(chunk -> {
                    lastChunkRef.set(chunk);
                    String text = chunk.getResult().getOutput().getText();
                    if (text != null) {
                        contentBuilder.append(text);
                        sseService.sendToken(this.chatSessionId, text);
                    }
                })
                .blockLast();

        ChatResponse lastChunk = lastChunkRef.get();
        Assert.notNull(lastChunk, "No streaming response received from model");

        // 用累积内容 + 最后一块的 toolCalls 重建 ChatResponse，供 execute() 使用
        String fullContent = contentBuilder.toString();
        List<AssistantMessage.ToolCall> toolCalls = lastChunk.getResult().getOutput().getToolCalls();
        AssistantMessage fullAssistantMessage = AssistantMessage.builder()
                .content(fullContent)
                .toolCalls(toolCalls)
                .build();
        this.lastChatResponse = new ChatResponse(List.of(new Generation(fullAssistantMessage)));

        //4.拿到完整的 AssistantMessage
        AssistantMessage output = this.lastChatResponse.getResult().getOutput();

        // 保存到数据库
        saveMessage(output);
        // SSE 推送给前端（AI_GENERATED_CONTENT）
        refreshPendingMessages();

        logToolCalls(toolCalls);

        // 如果有工具调用，则进入执行阶段
        return !toolCalls.isEmpty();
    }

    // 执行
    private void execute() {
        Assert.notNull(this.lastChatResponse, "Last chat client response cannot be null");
        //1.检查hasToolCall() 工具调用列表是否为空
        if (!this.lastChatResponse.hasToolCalls()) {
            return;
        }

        // HITL: 检查是否有高风险工具需要用户确认
        if (this.confirmationService != null) {
            List<AssistantMessage.ToolCall> toolCalls = this.lastChatResponse.getResult().getOutput().getToolCalls();
            for (AssistantMessage.ToolCall toolCall : toolCalls) {
                if (confirmationService.isHighRiskTool(toolCall.name())) {
                    boolean approved = requestUserConfirmation(toolCall);
                    if (!approved) {
                        log.info("用户拒绝了工具 {} 的执行", toolCall.name());
                        // 将拒绝信息作为 ToolResponseMessage 加入对话历史
                        ToolResponseMessage rejectionMessage = ToolResponseMessage.builder()
                                .responses(List.of(new ToolResponseMessage.ToolResponse(
                                        toolCall.id(),
                                        toolCall.name(),
                                        "用户拒绝了工具执行"
                                )))
                                .build();
                        this.chatMemory.add(this.chatSessionId, List.of(rejectionMessage));
                        saveMessage(rejectionMessage);
                        refreshPendingMessages();
                        return;
                    }
                }
            }
        }

        //2.构造prompt 获得上下文
        Prompt prompt = Prompt.builder()
                .messages(this.chatMemory.get(this.chatSessionId))
                .chatOptions(this.chatOptions)
                .build();

        //3.在lastChatResponse 中提取模型请求的工具调用（函数名 + 参数）
        //4.ToolExecutionResult 存完整的对话历史+工具返回消息
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, this.lastChatResponse);

        //5.清空chatmemory 再重新加入ToolExecutionResult
        this.chatMemory.clear(this.chatSessionId);
        this.chatMemory.add(this.chatSessionId, toolExecutionResult.conversationHistory());

        //6.执行完工具加入到对话历史的末尾 Spring AI 的约定是执行完工具后把结果追加到历史末尾。
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult
                .conversationHistory()
                .get(toolExecutionResult.conversationHistory().size() - 1);
        //7.打印多个工具的返回结果
        String collect = toolResponseMessage.getResponses()
                .stream()
                .map(resp -> "工具" + resp.name() + "的返回结果为：" + resp.responseData())
                .collect(Collectors.joining("\n"));

        log.info("工具调用结果：{}", collect);

        // 保存工具调用
        //8.持久化 + SSE推送 存模型的回复+工具调用
        //
        saveMessage(toolResponseMessage);
        refreshPendingMessages();

        if (toolResponseMessage.getResponses()
                .stream()
                .anyMatch(resp -> resp.name().equals("terminate"))) {
            this.agentState = AgentState.FINISHED;
            log.info("任务结束");
        }
    }

    /**
     * HITL: 通过 SSE 发送确认请求给前端，阻塞等待用户确认（60秒超时）
     */
    private boolean requestUserConfirmation(AssistantMessage.ToolCall toolCall) {
        String confirmationId = UUID.randomUUID().toString();
        CompletableFuture<Boolean> future = confirmationService.requestConfirmation(confirmationId);

        // 通过 SSE 发送 AWAITING_CONFIRMATION 事件给前端
        SseMessage confirmMessage = SseMessage.builder()
                .type(SseMessage.Type.AWAITING_CONFIRMATION)
                .payload(SseMessage.Payload.builder()
                        .confirmationId(confirmationId)
                        .toolName(toolCall.name())
                        .toolArgs(toolCall.arguments())
                        .build())
                .build();
        sseService.send(this.chatSessionId, confirmMessage);

        try {
            return future.get(60, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("确认请求超时, confirmationId: {}, toolName: {}", confirmationId, toolCall.name());
            return false;
        } catch (Exception e) {
            log.error("等待确认时发生异常, confirmationId: {}", confirmationId, e);
            return false;
        }
    }

    /**
     * 检查并压缩记忆：当对话历史超过阈值时，将旧消息压缩为摘要。
     * 如果 memoryCompressionService 为 null 或压缩失败，则跳过压缩。
     */
    private void compressMemoryIfNeeded() {
        if (this.memoryCompressionService == null) {
            return;
        }

        List<Message> messages = this.chatMemory.get(this.chatSessionId);
        if (messages.size() <= COMPRESSION_THRESHOLD) {
            return;
        }

        try {
            // 分离 SystemMessage 和非 SystemMessage
            List<Message> systemMessages = new ArrayList<>();
            List<Message> nonSystemMessages = new ArrayList<>();
            for (Message msg : messages) {
                if (msg instanceof SystemMessage) {
                    systemMessages.add(msg);
                } else {
                    nonSystemMessages.add(msg);
                }
            }

            // 如果非系统消息不够多，不压缩
            if (nonSystemMessages.size() <= RECENT_MESSAGES_TO_KEEP) {
                return;
            }

            // 取出需要压缩的旧消息（保留最近的 RECENT_MESSAGES_TO_KEEP 条）
            int splitIndex = nonSystemMessages.size() - RECENT_MESSAGES_TO_KEEP;
            List<Message> oldMessages = nonSystemMessages.subList(0, splitIndex);
            List<Message> recentMessages = nonSystemMessages.subList(splitIndex, nonSystemMessages.size());

            // 调用 LLM 压缩旧消息
            String summary = this.memoryCompressionService.compressMemory(oldMessages, this.chatClient);
            if (summary == null || summary.isBlank()) {
                log.warn("记忆压缩返回空结果，跳过压缩");
                return;
            }

            // 重建消息列表：系统消息 + 摘要（作为 SystemMessage）+ 最近消息
            List<Message> compressedMessages = new ArrayList<>();
            compressedMessages.addAll(systemMessages);
            compressedMessages.add(new SystemMessage("[以下是之前对话的摘要]\n" + summary));
            compressedMessages.addAll(recentMessages);

            // 替换 chatMemory 中的消息
            this.chatMemory.clear(this.chatSessionId);
            this.chatMemory.add(this.chatSessionId, compressedMessages);

            log.info("记忆压缩成功：{} 条消息 -> {} 条消息（含摘要）",
                    messages.size(), compressedMessages.size());
        } catch (Exception e) {
            log.warn("记忆压缩过程中发生异常，跳过压缩继续运行: {}", e.getMessage());
        }
    }

    /**
     * 判断用户消息是否足够复杂，需要规划阶段。
     * 简单的问候语或短消息不需要规划。
     */
    private boolean needsPlanning(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return false;
        }
        // 包含问号或长度超过 20 个字符的消息视为复杂消息
        return userMessage.contains("?") || userMessage.contains("？") || userMessage.length() > 20;
    }

    /**
     * 获取最近一条用户消息的内容。
     */
    private String getLatestUserMessage() {
        List<Message> messages = this.chatMemory.get(this.chatSessionId);
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg instanceof UserMessage userMessage) {
                return userMessage.getText();
            }
        }
        return null;
    }

    /**
     * 规划阶段：在 Agent 执行循环之前，先让 LLM 生成一个简要的执行计划。
     * 计划结果通过 SSE 的 AI_PLANNING 类型发送给前端，并作为上下文注入后续 think() 调用。
     * 如果规划失败，静默跳过，不影响主流程。
     */
    private void plan() {
        try {
            String userMessage = getLatestUserMessage();
            if (!needsPlanning(userMessage)) {
                log.info("[Plan] 消息较简单，跳过规划阶段");
                return;
            }

            String planPrompt = String.format("""
                    基于用户的问题，创建一个简要的执行计划。
                    你需要考虑：需要哪些信息？可能使用哪些工具？
                    请用 2-3 个要点回答，保持简洁。

                    用户问题：%s

                    可用的知识库：%s
                    """, userMessage, this.availableKbs);

            String planResponse = this.chatClient.prompt()
                    .user(planPrompt)
                    .call()
                    .content();

            if (StringUtils.hasText(planResponse)) {
                this.planText = planResponse;
                log.info("[Plan] 执行计划：{}", planResponse);

                // 通过 SSE 发送规划状态给前端
                SseMessage sseMessage = SseMessage.builder()
                        .type(SseMessage.Type.AI_PLANNING)
                        .payload(SseMessage.Payload.builder()
                                .statusText(planResponse)
                                .build())
                        .build();
                sseService.send(this.chatSessionId, sseMessage);
            }
        } catch (Exception e) {
            log.warn("[Plan] 规划阶段失败，跳过: {}", e.getMessage());
            // 规划失败不影响主流程
        }
    }

    // 单个步骤模板
    private void step() {
        compressMemoryIfNeeded();
        if (think()) {
            execute();
        } else { // 没有工具调用
            agentState = AgentState.FINISHED;
        }
    }

    /**
     * 在 Agent 运行开始时，将长期记忆注入到 chatMemory 中作为 SystemMessage。
     * 如果 longTermMemoryService 为 null 或无记忆，则跳过。
     */
    private void injectLongTermMemory() {
        if (this.longTermMemoryService == null) {
            return;
        }
        try {
            String memoriesText = this.longTermMemoryService.formatMemoriesForPrompt(this.agentId);
            if (memoriesText != null && !memoriesText.isBlank()) {
                this.chatMemory.add(this.chatSessionId, new SystemMessage(memoriesText));
                log.info("[LongTermMemory] 已注入长期记忆到会话上下文, agentId={}", this.agentId);
            }
        } catch (Exception e) {
            log.warn("[LongTermMemory] 注入长期记忆失败，跳过: {}", e.getMessage());
        }
    }

    /**
     * 在 Agent 运行结束后，从本次对话中提取关键信息并持久化为长期记忆。
     * 如果 longTermMemoryService 为 null，则跳过。
     */
    private void persistLongTermMemory() {
        if (this.longTermMemoryService == null) {
            return;
        }
        try {
            List<Message> messages = this.chatMemory.get(this.chatSessionId);
            this.longTermMemoryService.extractAndSaveMemories(
                    this.agentId, this.chatSessionId, messages, this.chatClient);
        } catch (Exception e) {
            log.warn("[LongTermMemory] 持久化长期记忆失败，跳过: {}", e.getMessage());
        }
    }

    // 运行
    public void run() {
        if (agentState != AgentState.IDLE) {
            throw new IllegalStateException("Agent is not idle");
        }

        try {
            // 注入长期记忆：在 Agent Loop 之前，将跨会话的关键信息注入上下文
            injectLongTermMemory();

            // 规划阶段：在 Agent Loop 之前执行
            plan();

            for (int i = 0; i < MAX_STEPS && agentState != AgentState.FINISHED; i++) {
                // 当前步骤，用于实现 Agent Loop
                int currentStep = i + 1;
                step();
                if (currentStep >= MAX_STEPS) {
                    agentState = AgentState.FINISHED;
                    log.warn("Max steps reached, stopping agent");
                }
            }
            agentState = AgentState.FINISHED;
        } catch (Exception e) {
            agentState = AgentState.ERROR;
            log.error("Error running agent", e);
            throw new RuntimeException("Error running agent", e);
        } finally {
            // 持久化长期记忆：无论成功或失败，尝试从对话中提取关键信息
            persistLongTermMemory();
        }
    }

    @Override
    public String toString() {
        return "ChatMind {" +
                "name = " + name + ",\n" +
                "description = " + description + ",\n" +
                "agentId = " + agentId + ",\n" +
                "systemPrompt = " + systemPrompt + "}";
    }
}
