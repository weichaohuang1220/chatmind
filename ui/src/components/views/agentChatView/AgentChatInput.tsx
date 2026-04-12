import React, { useState } from "react";
import { Sender } from "@ant-design/x";

interface AgentChatInputProps {
  onSend: (message: string) => void;
  disabled?: boolean;
}

const AgentChatInput: React.FC<AgentChatInputProps> = ({
  onSend,
  disabled = false,
}) => {
  const [message, setMessage] = useState("");

  return (
    <Sender
      onSubmit={() => {
        if (!message.trim()) return;
        onSend(message.trim());
        setMessage("");
      }}
      placeholder={disabled ? "加载中..." : "输入消息..."}
      value={message}
      onChange={setMessage}
      disabled={disabled}
    />
  );
};

export default AgentChatInput;
