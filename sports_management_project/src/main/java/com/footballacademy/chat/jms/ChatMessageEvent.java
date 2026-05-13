package com.footballacademy.chat.jms;

import java.io.Serializable;

public
record ChatMessageEvent(Long conversationId, Long senderId, Long receiverId, String content, String clientTempId) implements Serializable {
}
