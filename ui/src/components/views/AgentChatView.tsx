import React, { useCallback, useEffect, useRef, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { message as antdMessage, Modal } from "antd";
import AgentChatHistory from "./agentChatView/AgentChatHistory.tsx";
import AgentChatInput from "./agentChatView/AgentChatInput.tsx";
import {
  createChatMessage,
  getChatMessagesBySessionId,
  getChatSession,
  confirmAction,
} from "../../api/api.ts";
import { useAgents } from "../../hooks/useAgents.ts";
import EmptyAgentChatView from "./agentChatView/EmptyAgentChatView.tsx";
import type { ChatMessageVO, SseMessage, SseMessageType } from "../../types";

const AgentChatView: React.FC = () => {
  const { chatSessionId } = useParams<{ chatSessionId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const { state } = location;

  const [loading, setLoading] = useState(false);
  const { agents } = useAgents();

  const [messages, setMessages] = useState<ChatMessageVO[]>([]);
  const [pendingConfirmation, setPendingConfirmation] = useState<{
    confirmationId: string;
    toolName: string;
    toolArgs: string;
  } | null>(null);

  // agentId: 优先从 route state 获取（导航时立即可用），兜底从 session API 异步加载
  const [agentId, setAgentId] = useState<string>(state?.agentId ?? "");

  // 标记 SSE 是否已建立握手
  const [sseConnected, setSseConnected] = useState(false);

  // 防止 initMessage 被重复发送的 ref
  const initMessageSentRef = useRef(false);

  const addMessage = (message: ChatMessageVO) => {
    setMessages((prev) => [...prev, message]);
  };

  const getChatMessages = useCallback(async () => {
    if (!chatSessionId) return;
    const resp = await getChatMessagesBySessionId(chatSessionId);
    setMessages(resp.chatMessages);
  }, [chatSessionId]);

  // 切换 session 时：重置状态，加载消息，补充 agentId
  useEffect(() => {
    if (!chatSessionId) return;
    initMessageSentRef.current = false;
    setSseConnected(false);
    setMessages([]);
    setStreamingContent("");

    // 加载历史消息
    getChatMessages().then();

    // 如果 state 里没有 agentId，就从 session API 获取
    if (!state?.agentId) {
      getChatSession(chatSessionId)
        .then((resp) => setAgentId(resp.chatSession.agentId))
        .catch(() => {});
    }
  }, [chatSessionId]);

  // ── SSE 连接 ──────────────────────────────────────────────────────────────
  const [displayAgentStatus, setDisplayAgentStatus] = useState(false);
  const [agentStatusText, setAgentStatusText] = useState("");
  const [agentStatusType, setAgentStatusType] = useState<
    SseMessageType | undefined
  >(undefined);
  const [streamingContent, setStreamingContent] = useState("");

  useEffect(() => {
    if (!chatSessionId) return;

    const es = new EventSource(
      `http://localhost:8080/sse/connect/${chatSessionId}`,
    );

    // init 事件 = SSE 握手完成，可以安全地发送消息触发 AI
    es.addEventListener("init", () => {
      setSseConnected(true);
    });

    es.addEventListener("message", (event) => {
      const msg = JSON.parse(event.data) as SseMessage;
      if (msg.type === "AI_TOKEN") {
        setStreamingContent((prev) => prev + (msg.payload.token ?? ""));
      } else if (msg.type === "AI_GENERATED_CONTENT") {
        setStreamingContent("");
        addMessage(msg.payload.message);
      } else if (msg.type === "AI_PLANNING") {
        setDisplayAgentStatus(true);
        setAgentStatusText(msg.payload.statusText);
        setAgentStatusType("AI_PLANNING");
      } else if (msg.type === "AI_THINKING") {
        setDisplayAgentStatus(true);
        setAgentStatusText(msg.payload.statusText);
        setAgentStatusType("AI_THINKING");
      } else if (msg.type === "AI_EXECUTING") {
        setDisplayAgentStatus(true);
        setAgentStatusText(msg.payload.statusText);
        setAgentStatusType("AI_EXECUTING");
      } else if (msg.type === "AI_DONE") {
        setStreamingContent("");
        setDisplayAgentStatus(false);
        setAgentStatusText("");
        setAgentStatusType(undefined);
      } else if (msg.type === "AWAITING_CONFIRMATION") {
        setPendingConfirmation({
          confirmationId: msg.payload.confirmationId!,
          toolName: msg.payload.toolName!,
          toolArgs: msg.payload.toolArgs!,
        });
      }
    });

    es.onerror = (error) => {
      console.error("SSE error:", error);
    };

    return () => {
      setSseConnected(false);
      es.close();
    };
  }, [chatSessionId]);

  // ── SSE 建立后发送初始消息 ────────────────────────────────────────────────
  // EmptyAgentChatView 导航时将消息存入 state.initMessage，
  // 此处等 SSE ready + agentId 有值后才发送，确保后端 SSE emitter 已注册。
  useEffect(() => {
    if (!sseConnected) return;
    if (!chatSessionId) return;
    if (!agentId) return;
    if (!state?.initMessage) return;
    if (initMessageSentRef.current) return;

    initMessageSentRef.current = true;
    const content: string = state.initMessage;

    createChatMessage({
      agentId,
      sessionId: chatSessionId,
      role: "user",
      content,
    })
      .then(() => {
        getChatMessages();
        // 清除 state.initMessage，防止页面刷新时重复发送
        navigate(location.pathname, {
          replace: true,
          state: { agentId },
        });
      })
      .catch(() => {
        antdMessage.error("发送消息失败，请重试");
        initMessageSentRef.current = false;
      });
  }, [sseConnected, agentId, chatSessionId]);

  // ── HITL 确认操作 ──────────────────────────────────────────────────────
  const handleConfirmation = async (approved: boolean) => {
    if (!pendingConfirmation) return;
    try {
      await confirmAction(pendingConfirmation.confirmationId, approved);
    } catch {
      antdMessage.error("确认操作失败");
    } finally {
      setPendingConfirmation(null);
    }
  };

  // ── 用户在聊天页输入框发送消息 ───────────────────────────────────────────
  const handleSendMessage = async (content: string) => {
    if (!content.trim()) return;
    if (!chatSessionId) return;

    if (!agentId) {
      antdMessage.warning("会话信息加载中，请稍后再试");
      return;
    }

    setLoading(true);
    try {
      await createChatMessage({
        agentId,
        sessionId: chatSessionId,
        role: "user",
        content: content.trim(),
      });
      await getChatMessages();
    } catch {
      antdMessage.error("发送失败，请重试");
    } finally {
      setLoading(false);
    }
  };

  // ── 渲染 ──────────────────────────────────────────────────────────────────
  if (!chatSessionId) {
    return (
      <EmptyAgentChatView
        agents={agents}
        loading={loading}
        handleSendMessage={() => {}}
      />
    );
  }

  return (
    <div className="flex flex-col h-full">
      <AgentChatHistory
        messages={messages}
        streamingContent={streamingContent}
        displayAgentStatus={displayAgentStatus}
        agentStatusText={agentStatusText}
        agentStatusType={agentStatusType}
      />
      <div className="border-t border-gray-200 p-4 bg-white">
        <AgentChatInput
          onSend={handleSendMessage}
          disabled={!agentId || loading}
        />
      </div>
      <Modal
        open={!!pendingConfirmation}
        title="⚠️ 高危操作确认"
        okText="批准执行"
        cancelText="拒绝"
        onOk={() => handleConfirmation(true)}
        onCancel={() => handleConfirmation(false)}
        closable={false}
        maskClosable={false}
      >
        <div className="py-4">
          <p className="text-gray-700 mb-2">
            Agent 请求执行以下高危工具，需要您的确认：
          </p>
          <div className="bg-gray-50 rounded-lg p-3 mb-3">
            <p className="font-medium text-gray-900">
              🔧 工具: {pendingConfirmation?.toolName}
            </p>
            <p className="text-sm text-gray-600 mt-1">
              📋 参数: {pendingConfirmation?.toolArgs}
            </p>
          </div>
          <p className="text-orange-600 text-sm">
            请确认是否允许执行此操作。拒绝后 Agent 将跳过该工具。
          </p>
        </div>
      </Modal>
    </div>
  );
};

export default AgentChatView;
