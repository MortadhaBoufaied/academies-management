package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_reads", uniqueConstraints =
@UniqueConstraint(columnNames = {
    "message_id", "user_id"
}))
public
class MessageRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "message_id", nullable = false)
    private Long messageId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt = LocalDateTime.now();
    public MessageRead() {
    }
    public MessageRead(Long messageId, Long userId) {
        this.messageId = messageId;
        this.userId = userId;
        this.readAt = LocalDateTime.now();
    }
    public MessageRead(Long messageId, Long userId, LocalDateTime readAt) {
        this.messageId = messageId;
        this.userId = userId;
        this.readAt = readAt != null ? readAt : LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getMessageId() {
        return messageId;
    }
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public LocalDateTime getReadAt() {
        return readAt;
    }
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
