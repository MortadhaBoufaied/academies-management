package com.footballacademy.DTO;

import com.footballacademy.model.Notification;
import java.util.List;

public
class SendNotificationRequest {
    private String title;
    private String content;
    private Notification.Category category;
    private List<Long> userIds;
    // If null/empty, send to all in category
    private Boolean sendToAll;
    // true = all in category, false = specific users
    private Long academyId;
    // SUPER_ADMIN may target one academy; ADMIN is forced to own academy
    private Boolean global;
    // SUPER_ADMIN-only broadcast across academies
    public SendNotificationRequest() {
    }
    public SendNotificationRequest(String title, String content, Notification.Category category, List<Long> userIds, Boolean sendToAll) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.userIds = userIds;
        this.sendToAll = sendToAll;
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
    public Notification.Category getCategory() {
        return category;
    }
    public void setCategory(Notification.Category category) {
        this.category = category;
    }
    public List<Long> getUserIds() {
        return userIds;
    }
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
    public Boolean getSendToAll() {
        return sendToAll;
    }
    public void setSendToAll(Boolean sendToAll) {
        this.sendToAll = sendToAll;
    }
    public Long getAcademyId() {
        return academyId;
    }
    public void setAcademyId(Long academyId) {
        this.academyId = academyId;
    }
    public Boolean getGlobal() {
        return global;
    }
    public void setGlobal(Boolean global) {
        this.global = global;
    }
}
