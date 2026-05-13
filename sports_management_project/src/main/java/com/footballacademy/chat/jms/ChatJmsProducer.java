package com.footballacademy.chat.jms;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public
class ChatJmsProducer {
    private final JmsTemplate jmsTemplate;
    private final ChatMessageHandler handler;
    public ChatJmsProducer(JmsTemplate jmsTemplate, ChatMessageHandler handler) {
        this.jmsTemplate = jmsTemplate;
        this.handler = handler;
    }
    public void enqueue(ChatMessageEvent event) {
        try {
            jmsTemplate.convertAndSend(ChatJmsConfig.QUEUE_CHAT_MESSAGES, event);
        } catch (Exception ex) {
            // Fallback path
            handler.handle(event);
        }
    }
}
