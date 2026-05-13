package com.footballacademy.DTO.notification;

import com.footballacademy.model.Notification;
import java.time.LocalDateTime;

public
class NotificationViewDto {
    private Long id;
    private Long campaignId;
    private Long userId;
    private Long createdBy;
    private String senderName;
    private String title;
    private String content;
    private String contentHtml;
    private String category;
    private boolean readStatus;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    public static NotificationViewDto from(Notification notification, String senderName) {
        NotificationViewDto dto = new NotificationViewDto();
        dto.id = notification.getId();
        dto.campaignId = notification.getCampaignId();
        dto.userId = notification.getUserId();
        dto.createdBy = notification.getCreatedBy();
        dto.senderName = senderName;
        dto.title = notification.getTitle();
        dto.content = notification.getContent();
        dto.contentHtml = notification.getContentHtml();
        dto.category = notification.getCategory() != null ? notification.getCategory() .name() : null;
        dto.readStatus = notification.isRead();
        dto.createdAt = notification.getCreatedAt();
        dto.readAt = notification.getReadAt();
        return dto;
    }
    public Long getId() {
        return id;
    }
    public Long getCampaignId() {
        return campaignId;
    }
    public Long getUserId() {
        return userId;
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
    public String getContent() {
        return content;
    }
    public String getContentHtml() {
        return contentHtml;
    }
    public String getCategory() {
        return category;
    }
    public boolean isReadStatus() {
        return readStatus;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getReadAt() {
        return readAt;
    }
}
