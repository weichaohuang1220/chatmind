import React, { useState, useMemo, useRef, useEffect } from "react";
import { Card, Space, Typography, Select, message as antdMessage } from "antd";
import {
  BulbOutlined,
  MessageOutlined,
  RobotOutlined,
  DownOutlined,
} from "@ant-design/icons";
import { Sender } from "@ant-design/x";
import { useNavigate, useLocation } from "react-router-dom";
import {
  type AgentVO,
  createChatSession,
} from "../../../api/api.ts";
import { getAgentEmoji } from "../../../utils";
import { useChatSessions } from "../../../hooks/useChatSessions.ts";

const { Title, Text } = Typography;

interface DefaultAgentChatViewProps {
  handleSendMessage: (message: string) => void;
  loading: boolean;
  agents: AgentVO[];
}

const EmptyAgentChatView: React.FC<DefaultAgentChatViewProps> = ({
  loading,
  agents,
}) => {
  const [message, setMessage] = useState("");
  const [cardLoading, setCardLoading] = useState(false);
  const senderRef = useRef<HTMLDivElement>(null);

  const navigate = useNavigate();
  const location = useLocation();
  const routeState = location.state as { selectedAgentId?: string } | null;

  // 优先使用 route state 传入的 agentId（侧栏点击），否则 null
  const [selectedAgentId, setSelectedAgentId] = useState<string | null>(
    routeState?.selectedAgentId ?? null,
  );
  const { refreshChatSessions } = useChatSessions();

  // 侧栏切换 agent 时同步 selectedAgentId
  useEffect(() => {
    if (routeState?.selectedAgentId) {
      setSelectedAgentId(routeState.selectedAgentId);
    }
  }, [routeState?.selectedAgentId]);

  // 为每个 agent 生成 emoji
  const agentsWithEmoji = useMemo(() => {
    return agents.map((agent) => ({
      ...agent,
      emoji: getAgentEmoji(agent.id),
    }));
  }, [agents]);

  // 计算实际选中的 agent ID
  const effectiveAgentId = useMemo(() => {
    if (selectedAgentId) {
      return selectedAgentId;
    }
    return agents.length > 0 ? agents[0].id : null;
  }, [selectedAgentId, agents]);

  /**
   * 创建会话并导航到聊天页面。
   * 关键：不在这里调用 createChatMessage，而是通过 route state 把 initMessage 传给
   * AgentChatView，由它在 SSE 连接建立之后再发送，避免 AI 任务比 SSE 先启动的竞态。
   */
  const navigateToNewSession = async (title: string, initMessage?: string) => {
    if (!effectiveAgentId) {
      antdMessage.warning("请先创建一个智能体助手");
      return;
    }
    setCardLoading(true);
    try {
      const response = await createChatSession({
        agentId: effectiveAgentId,
        title,
      });
      await refreshChatSessions();
      navigate(`/chat/${response.chatSessionId}`, {
        state: {
          initMessage: initMessage ?? "",
          agentId: effectiveAgentId,
        },
      });
    } catch {
      antdMessage.error("创建会话失败，请重试");
    } finally {
      setCardLoading(false);
    }
  };

  const focusSenderInput = () => {
    senderRef.current?.querySelector("textarea")?.focus();
  };

  return (
    <div className="flex flex-col h-full">
      {/* Agent 选择器 - 顶部 */}
      {agents.length > 0 && (
        <div className="border-b border-gray-200 bg-white px-4 py-3">
          <div className="flex items-center justify-start">
            <Select
              value={effectiveAgentId}
              onChange={(value) => setSelectedAgentId(value)}
              style={{ width: 200 }}
              className="agent-selector"
              suffixIcon={<DownOutlined className="text-gray-400" />}
              placeholder="选择智能体助手"
              optionRender={(option) => (
                <div className="flex items-center gap-2">
                  <span className="text-lg">
                    {agentsWithEmoji.find((a) => a.id === option.value)?.emoji}
                  </span>
                  <span className="text-sm">{option.label}</span>
                </div>
              )}
              options={agentsWithEmoji.map((agent) => ({
                value: agent.id,
                label: agent.name,
              }))}
            />
          </div>
        </div>
      )}
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="max-w-2xl w-full space-y-6">
          <div className="text-center mb-8">
            <Title level={2} className="mb-2">
              开始新的对话
            </Title>
            <Text type="secondary" className="text-base">
              选择一个智能体助手开始聊天，或直接发送消息创建新会话
            </Text>
          </div>
          <Space orientation="vertical" size="large" className="w-full">
            <Card
              hoverable
              loading={cardLoading}
              className="cursor-pointer transition-all hover:shadow-lg"
              onClick={() => navigateToNewSession("智能对话")}
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-purple-400 flex items-center justify-center">
                  <RobotOutlined className="text-white text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    智能对话
                  </Title>
                  <Text type="secondary">
                    与 AI 助手进行智能对话，获取帮助和建议
                  </Text>
                </div>
              </Space>
            </Card>

            <Card
              hoverable
              loading={cardLoading}
              className="cursor-pointer transition-all hover:shadow-lg"
              onClick={() => navigateToNewSession("知识问答")}
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-green-400 to-teal-400 flex items-center justify-center">
                  <BulbOutlined className="text-white text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    知识问答
                  </Title>
                  <Text type="secondary">
                    基于知识库进行问答，获取准确的信息
                  </Text>
                </div>
              </Space>
            </Card>

            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
              onClick={focusSenderInput}
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-orange-400 to-red-400 flex items-center justify-center">
                  <MessageOutlined className="text-white text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    快速开始
                  </Title>
                  <Text type="secondary">
                    在下方输入框输入消息，立即开始对话
                  </Text>
                </div>
              </Space>
            </Card>
          </Space>
        </div>
      </div>
      <div className="border-t border-gray-200 bg-white">
        <div className="px-4 pb-4 pt-4" ref={senderRef}>
          <Sender
            onSubmit={async () => {
              if (!message.trim()) return;
              const trimmed = message.trim();
              setMessage("");
              await navigateToNewSession(trimmed.slice(0, 20), trimmed);
            }}
            value={message}
            loading={loading || cardLoading}
            placeholder="输入消息开始对话..."
            onChange={(value) => {
              setMessage(value);
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default EmptyAgentChatView;
