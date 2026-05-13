package com.footballacademy.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class AuditLogger {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public enum AuditAction {
        CREATE, UPDATE, DELETE, READ, LOGIN, LOGOUT, PAYMENT, NOTIFICATION, EXPORT, IMPORT, SETTINGS_CHANGE
    }
    public enum AuditStatus {
        SUCCESS, FAILURE, PARTIAL
    }
    public static class AuditLogEntry {
        private final String id;
        private final LocalDateTime timestamp;
        private final String action;
        private final String status;
        private final String username;
        private final String userId;
        private final String resource;
        private final String resourceId;
        private final String details;
        private final String ipAddress;
        private final String userAgent;
        public AuditLogEntry(Builder builder) {
            this.id = builder.id;
            this.timestamp = builder.timestamp;
            this.action = builder.action;
            this.status = builder.status;
            this.username = builder.username;
            this.userId = builder.userId;
            this.resource = builder.resource;
            this.resourceId = builder.resourceId;
            this.details = builder.details;
            this.ipAddress = builder.ipAddress;
            this.userAgent = builder.userAgent;
        }
        public String getId() {
            return id;
        }
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        public String getAction() {
            return action;
        }
        public String getStatus() {
            return status;
        }
        public String getUsername() {
            return username;
        }
        public String getUserId() {
            return userId;
        }
        public String getResource() {
            return resource;
        }
        public String getResourceId() {
            return resourceId;
        }
        public String getDetails() {
            return details;
        }
        public String getIpAddress() {
            return ipAddress;
        }
        public String getUserAgent() {
            return userAgent;
        }
        @Override
        public String toString() {
            return String.format("[%s] %s - %s - %s - %s - %s - %s - %s - %s - %s - %s", timestamp.format(TIMESTAMP_FORMATTER), id, action, status, username, userId, resource, resourceId, details, ipAddress, userAgent);
        }
        public static class Builder {
            private String id = UUID.randomUUID().toString();
            private LocalDateTime timestamp = LocalDateTime.now();
            private String action;
            private String status;
            private String username = "system";
            private String userId = "system";
            private String resource;
            private String resourceId;
            private String details;
            private String ipAddress;
            private String userAgent;
            public Builder id(String id) {
                this.id = id;
                return this;
            }
            public Builder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            public Builder action(AuditAction action) {
                this.action = action.name();
                return this;
            }
            public Builder action(String action) {
                this.action = action;
                return this;
            }
            public Builder status(AuditStatus status) {
                this.status = status.name();
                return this;
            }
            public Builder status(String status) {
                this.status = status;
                return this;
            }
            public Builder username(String username) {
                this.username = username;
                return this;
            }
            public Builder userId(String userId) {
                this.userId = userId;
                return this;
            }
            public Builder resource(String resource) {
                this.resource = resource;
                return this;
            }
            public Builder resourceId(String resourceId) {
                this.resourceId = resourceId;
                return this;
            }
            public Builder details(String details) {
                this.details = details;
                return this;
            }
            public Builder ipAddress(String ipAddress) {
                this.ipAddress = ipAddress;
                return this;
            }
            public Builder userAgent(String userAgent) {
                this.userAgent = userAgent;
                return this;
            }
            public AuditLogEntry build() {
                return new AuditLogEntry(this);
            }
        }
    }
    public static AuditLogEntry.Builder createEntry() {
        return new AuditLogEntry.Builder();
    }
    public static String formatTimestamp(LocalDateTime timestamp) {
        return timestamp.format(TIMESTAMP_FORMATTER);
    }
    public static String generateAuditId() {
        return "AUDIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
