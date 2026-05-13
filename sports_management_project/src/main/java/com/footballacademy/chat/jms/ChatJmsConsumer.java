package com.footballacademy.chat.jms;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public
class ChatJmsConsumer {
    private final ChatMessageHandler handler;
    public ChatJmsConsumer(ChatMessageHandler handler) {
        this.handler = handler;
    }
    @JmsListener(destination = ChatJmsConfig.QUEUE_CHAT_MESSAGES, containerFactory = "jmsListenerContainerFactory")
    public void onMessage(ChatMessageEvent event) {
        handler.handle(event);
    }
}
