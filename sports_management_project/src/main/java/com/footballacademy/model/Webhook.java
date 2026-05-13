package com.footballacademy.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Webhook entity for n8n integration.
 *
 * Stores webhook configurations for external automation
 * (e.g. USER_CREATED, PAYMENT_COMPLETED, ACTIVITY_CREATED).
 */
@Entity
@Table(name = "webhooks")
public class Webhook {

    // =====================================================
    // === PRIMARY KEY
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // === CORE CONFIGURATION
    // =====================================================

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String eventType;

    /**
     * Whether the webhook is enabled.
     */
    @Column(nullable = false)
    private Boolean active;

    /**
     * HTTP method (POST, GET, PUT, DELETE).
     */
    @Column(nullable = false)
    private String httpMethod;

    /**
     * Optional JSON string of custom headers.
     */
    @Lob
    private String headers;

    /**
     * Authentication configuration
     * (API key, bearer token, etc.).
     */
    @Lob
    private String authentication;

    // =====================================================
    // === AUDIT FIELDS
    // =====================================================

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    @Column(name = "trigger_count")
    private Integer triggerCount;

    // =====================================================
    // === JPA LIFECYCLE
    // =====================================================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.triggerCount = 0;
        this.active = Boolean.TRUE.equals(this.active);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // === DOMAIN LOGIC
    // =====================================================

    public void incrementTriggerCount() {
        this.triggerCount = (this.triggerCount == null) ? 1 : this.triggerCount + 1;
        this.lastTriggeredAt = LocalDateTime.now();
    }

    // =====================================================
    // === GETTERS & SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public Integer getTriggerCount() {
        return triggerCount;
    }

    public void setTriggerCount(Integer triggerCount) {
        this.triggerCount = triggerCount;
    }
}
