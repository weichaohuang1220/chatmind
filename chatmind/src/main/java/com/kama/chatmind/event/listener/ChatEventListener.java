package com.kama.chatmind.event.listener;

import com.kama.chatmind.agent.ChatMind;
import com.kama.chatmind.agent.ChatMindFactory;
import com.kama.chatmind.event.ChatEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChatEventListener {

    private final ChatMindFactory jChatMindFactory;

    @Async
    @EventListener
    public void handle(ChatEvent event) {
        // 创建一个 Agent 实例处理聊天事件
        ChatMind jChatMind = jChatMindFactory.create(event.getAgentId(), event.getSessionId());
        jChatMind.run();
    }
}
