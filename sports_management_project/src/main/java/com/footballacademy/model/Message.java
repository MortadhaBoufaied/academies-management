package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_message_conversation_id", columnList = "conversation_id"),
    @Index(name = "idx_message_sender_id", columnList = "sender_id"),
    @Index(name = "idx_message_receiver_id", columnList = "receiver_id"),
    @Index(name = "idx_message_timestamp", columnList = "timestamp"),
    @Index(name = "idx_message_is_read", columnList = "is_read"),
    @Index(name = "idx_message_client_temp_id", columnList = "client_temp_id")
})
public
class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long conversationId;
    @Column(nullable = false)
    private Long senderId;
    // Nullable for group/division messages
    private Long receiverId;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
    // ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ FIX: avoid SQL keyword "read"
    @Column(name = "is_read", nullable = false)
    private boolean read = false;
    @Column(name = "client_temp_id")
    private String clientTempId;
    protected Message() {
        // JPA
    }
    public Message(Long conversationId, Long senderId, Long receiverId, String content, String clientTempId) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.clientTempId = clientTempId;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }
    // ---------------- getters ----------------
    public Long getId() {
        return id;
    }
    public Long getConversationId() {
        return conversationId;
    }
    public Long getSenderId() {
        return senderId;
    }
    public Long getReceiverId() {
        return receiverId;
    }
    public String getContent() {
        return content;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public boolean isRead() {
        return read;
    }
    public String getClientTempId() {
        return clientTempId;
    }
    // ---------------- setters (REQUIRED) ----------------      // ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ THIS FIXES YOUR ERROR
    public void setRead(boolean read) {
        this.read = read;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
