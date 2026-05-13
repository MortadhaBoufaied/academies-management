package com.footballacademy.DTO.chat;

public
record SendMessageAck(boolean queued, String clientTempId) {
}
