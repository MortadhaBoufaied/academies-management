package com.footballacademy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**  * WebhookLog entity for tracking webhook execution history.  */
@Entity
@Table(name = "webhook_logs")
public
class WebhookLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_id", foreignKey =
    @ForeignKey(name = "fk_log_webhook"))
    private Webhook webhook;
    @Column(nullable = false)
    private String eventType;
    @Column(nullable = false)
    private String payload;
    // JSON string of the payload sent
    @Column(nullable = false)
    private Integer statusCode;
    // HTTP response status code
    private String responseBody;
    // Response from webhook endpoint
    @Column(nullable = false)
    private Boolean success;
    private String errorMessage;
    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    // Response time in milliseconds
    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
    }
    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Webhook getWebhook() {
        return webhook;
    }
    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }
    public String getEventType() {
        return eventType;
    }
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    public String getPayload() {
        return payload;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }
    public Integer getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    public String getResponseBody() {
        return responseBody;
    }
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
    public Boolean getSuccess() {
        return success;
    }
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
    public Long getResponseTimeMs() {
        return responseTimeMs;
    }
    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
}
