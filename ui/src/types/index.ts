export type MessageType = "user" | "assistant" | "system" | "tool";

export interface KnowledgeBase {
  knowledgeBaseId: string;
  name: string;
  description: string;
}

export interface ToolCall {
  id: string;
  type: string;
  name: string;
  arguments: string;
}

export interface ToolResponse {
  id: string;
  name: string;
  responseData: string;
}

export interface ChatMessageVOMetadata {
  toolCalls?: ToolCall[];
  toolResponse?: ToolResponse;
}

export interface ChatMessageVO {
  id: string;
  sessionId: string;
  role: MessageType;
  content: string;
  metadata?: ChatMessageVOMetadata;
}

export type SseMessageType =
  | "AI_GENERATED_CONTENT"
  | "AI_PLANNING"
  | "AI_THINKING"
  | "AI_EXECUTING"
  | "AI_DONE"
  | "AI_TOKEN"
  | "AWAITING_CONFIRMATION";

export interface SseMessagePayload {
  message: ChatMessageVO;
  statusText: string;
  done: boolean;
  token?: string;
  confirmationId?: string;
  toolName?: string;
  toolArgs?: string;
}

export interface SseMessageMetadata {
  chatMessageId: string;
}

export interface SseMessage {
  type: SseMessageType;
  payload: SseMessagePayload;
  metadata: SseMessageMetadata;
}
