package com.kama.chatmind.model.response;

import com.kama.chatmind.model.vo.ChatMessageVO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetChatMessagesResponse {
    private ChatMessageVO[] chatMessages;
}

