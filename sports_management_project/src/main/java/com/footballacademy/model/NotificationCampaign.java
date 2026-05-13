package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_campaigns", indexes = {
    @Index(name = "idx_notification_campaign_academy", columnList = "academy_id"),
    @Index(name = "idx_notification_campaign_created_by", columnList = "created_by"),
    @Index(name = "idx_notification_campaign_created_at", columnList = "created_at")
})
public
class NotificationCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;
    @Column(name = "targeting_mode", nullable = false, length = 40)
    private String targetingMode;
    @Column(name = "audience_summary", length = 255)
    private String audienceSummary;
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Notification.Category category;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", foreignKey =
    @ForeignKey(name = "fk_notification_campaign_academy"))
    private Academy academy;
    @PrePersist void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (category == null) {
            category = Notification.Category.GENERAL;
        }
        if (targetingMode == null || targetingMode.isBlank()) {
            targetingMode = "GENERAL";
        }
    }
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
    public String getTargetingMode() {
        return targetingMode;
    }
    public void setTargetingMode(String targetingMode) {
        this.targetingMode = targetingMode;
    }
    public String getAudienceSummary() {
        return audienceSummary;
    }
    public void setAudienceSummary(String audienceSummary) {
        this.audienceSummary = audienceSummary;
    }
    public Long getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    public Notification.Category getCategory() {
        return category;
    }
    public void setCategory(Notification.Category category) {
        this.category = category;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Academy getAcademy() {
        return academy;
    }
    public void setAcademy(Academy academy) {
        this.academy = academy;
    }
}
