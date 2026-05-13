package com.footballacademy.DTO.notification;

import java.time.LocalDateTime;

public
class NotificationCampaignStatsDto {
    private Long id;
    private Long academyId;
    private Long createdBy;
    private String senderName;
    private String title;
    private String contentPreview;
    private String targetingMode;
    private String audienceSummary;
    private String category;
    private int totalRecipients;
    private int readCount;
    private int unreadCount;
    private double readPercentage;
    private LocalDateTime createdAt;
    public NotificationCampaignStatsDto() {
    }
    public NotificationCampaignStatsDto(Long id, Long academyId, Long createdBy, String senderName, String title, String contentPreview, String targetingMode, String audienceSummary, String category, int totalRecipients, int readCount, int unreadCount, double readPercentage, LocalDateTime createdAt) {
        this.id = id;
        this.academyId = academyId;
        this.createdBy = createdBy;
        this.senderName = senderName;
        this.title = title;
        this.contentPreview = contentPreview;
        this.targetingMode = targetingMode;
        this.audienceSummary = audienceSummary;
        this.category = category;
        this.totalRecipients = totalRecipients;
        this.readCount = readCount;
        this.unreadCount = unreadCount;
        this.readPercentage = readPercentage;
        this.createdAt = createdAt;
    }
    public Long getId() {
        return id;
    }
    public Long getAcademyId() {
        return academyId;
    }
    public Long getCreatedBy() {
        return createdBy;
    }
    public String getSenderName() {
        return senderName;
    }
    public String getTitle() {
        return title;
    }
    public String getContentPreview() {
        return contentPreview;
    }
    public String getTargetingMode() {
        return targetingMode;
    }
    public String getAudienceSummary() {
        return audienceSummary;
    }
    public String getCategory() {
        return category;
    }
    public int getTotalRecipients() {
        return totalRecipients;
    }
    public int getReadCount() {
        return readCount;
    }
    public int getUnreadCount() {
        return unreadCount;
    }
    public double getReadPercentage() {
        return readPercentage;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
