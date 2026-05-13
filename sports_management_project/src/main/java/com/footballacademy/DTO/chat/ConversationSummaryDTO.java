package com.footballacademy.DTO.chat;

import java.time.LocalDateTime;
import java.util.List;

public
record ConversationSummaryDTO(Long id, String title, List<ParticipantDTO> participants, String lastMessage, LocalDateTime lastMessageAt, long unreadCount, LocalDateTime updatedAt) {
}
