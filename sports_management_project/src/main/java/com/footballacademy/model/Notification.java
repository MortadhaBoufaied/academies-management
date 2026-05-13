package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_academy", columnList = "academy_id"),
    @Index(name = "idx_notification_campaign_id", columnList = "campaign_id"),
    @Index(name = "idx_notification_category", columnList = "category"),
    @Index(name = "idx_notification_is_read", columnList = "is_read"),
    @Index(name = "idx_notification_created_at", columnList = "created_at"),
    @Index(name = "idx_notification_created_by", columnList = "created_by"),
    @Index(name = "idx_notification_scheduled_for", columnList = "scheduled_for"),
    @Index(name = "idx_notification_conversation_id", columnList = "conversation_id")
})
public
class Notification {
    public
    enum Category {
        PARENTS, FOOTBALLERS, TRAINERS, ADMIN, GENERAL
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;
    @Column(name = "is_read")
    private boolean isRead;
    @Column(name = "read_at")
    private LocalDateTime readAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    // Admin ID who created it
    @Column(name = "user_id")
    private Long userId;
    // Null if sent to multiple users
    @Column(name = "campaign_id")
    private Long campaignId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_notification_academy"))
    private Academy academy;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    @Column(name = "is_scheduled")
    private boolean isScheduled;
    // True for scheduled messages
    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;
    // When to send scheduled messages
    @Column(name = "conversation_id")
    private Long conversationId;
    // For message notifications          // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.isScheduled = false;
    }
    public Notification(String title, String content, Category category, Long userId, Long createdBy) {
        this();
        this.title = title;
        this.content = content;
        this.category = category;
        this.userId = userId;
        this.createdBy = createdBy;
    }
    // Getters et Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getContentHtml() {
        return contentHtml;
    }
    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }
    public boolean isRead() {
        return isRead;
    }
    public void setRead(boolean read) {
        isRead = read;
    }
    public LocalDateTime getReadAt() {
        return readAt;
    }
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getCampaignId() {
        return campaignId;
    }
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }
    public Academy getAcademy() {
        return academy;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
    public Long getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    public boolean isScheduled() {
        return isScheduled;
    }
    public void setScheduled(boolean scheduled) {
        isScheduled = scheduled;
    }
    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }
    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }
    public Long getConversationId() {
        return conversationId;
    }
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}
