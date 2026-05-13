package com.footballacademy.DTO.chat;

import java.time.LocalDateTime;

public
record ChatMessageDTO(Long id, Long conversationId, Long senderId, Long receiverId, String content, LocalDateTime timestamp, boolean read, String clientTempId) {
}
