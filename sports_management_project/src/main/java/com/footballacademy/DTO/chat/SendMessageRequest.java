package com.footballacademy.DTO.chat;

public
record SendMessageRequest(Long senderId, Long receiverId, String content, String clientTempId) {
}
