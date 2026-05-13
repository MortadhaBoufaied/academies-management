package com.footballacademy.controllers_rest.notification;

import com.footballacademy.model.Notification;
import com.footballacademy.services.notification.AdvancedNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications/advanced")
@RequiredArgsConstructor
public
class AdvancedNotificationController {
    private final AdvancedNotificationService advancedNotificationService;
    /**      * Send custom notification to a specific user      */
    @PostMapping("/send/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<?> sendCustomNotification(
    @PathVariable Long userId,
    @RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String message = request.get("message");
            String type = request.getOrDefault("type", "INFO");
            if (title == null || title.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Title is required"));
            }
            if (message == null || message.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Message is required"));
            } Notification notification = advancedNotificationService.sendCustomNotification(userId, title, message, type);
            return ResponseEntity.status(HttpStatus.CREATED) .body(notification);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to send notification: " + e.getMessage()));
        }
    }
    /**      * Send broadcast notification to all users      */
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendBroadcastNotification(
    @RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String message = request.get("message");
            String type = request.getOrDefault("type", "INFO");
            if (title == null || title.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Title is required"));
            }
            if (message == null || message.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Message is required"));
            } List<Notification> notifications = advancedNotificationService.sendBroadcastNotification(title, message, type);
            return ResponseEntity.status(HttpStatus.CREATED) .body(Map.of("count", notifications.size(), "notifications", notifications));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to send broadcast: " + e.getMessage()));
        }
    }
    /**      * Send notification to users with specific role      */
    @PostMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendNotificationByRole(
    @PathVariable String role,
    @RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String message = request.get("message");
            String type = request.getOrDefault("type", "INFO");
            if (title == null || title.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Title is required"));
            }
            if (message == null || message.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Message is required"));
            } List<Notification> notifications = advancedNotificationService.sendNotificationByRole(role, title, message, type);
            return ResponseEntity.status(HttpStatus.CREATED) .body(Map.of("role", role, "count", notifications.size(), "notifications", notifications));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to send role notification: " + e.getMessage()));
        }
    }
    /**      * Mark notification as read      */
    @PostMapping("/read/{notificationId}")
    public ResponseEntity<?> markAsRead(
    @PathVariable Long notificationId) {
        try {
            advancedNotificationService.markAsRead(notificationId);
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to mark as read: " + e.getMessage()));
        }
    }
    /**      * Mark all notifications as read for a user      */
    @PostMapping("/read-all/{userId}")
    public ResponseEntity<?> markAllAsRead(
    @PathVariable Long userId) {
        try {
            advancedNotificationService.markAllAsRead(userId);
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to mark all as read: " + e.getMessage()));
        }
    }
    /**      * Get unread notifications for a user      */
    @GetMapping("/unread/{userId}")
    public ResponseEntity<?> getUnreadNotifications(
    @PathVariable Long userId) {
        try {
            List<Notification> notifications = advancedNotificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(Map.of("count", notifications.size(), "notifications", notifications));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to get unread notifications: " + e.getMessage()));
        }
    }
    /**      * Get all notifications for a user      */
    @GetMapping("/all/{userId}")
    public ResponseEntity<?> getAllNotifications(
    @PathVariable Long userId) {
        try {
            List<Notification> notifications = advancedNotificationService.getAllNotifications(userId);
            return ResponseEntity.ok(Map.of("count", notifications.size(), "notifications", notifications));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to get notifications: " + e.getMessage()));
        }
    }
    /**      * Get notification statistics for a user      */
    @GetMapping("/statistics/{userId}")
    public ResponseEntity<?> getNotificationStatistics(
    @PathVariable Long userId) {
        try {
            Map<String, Object> statistics = advancedNotificationService.getNotificationStatistics(userId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to get statistics: " + e.getMessage()));
        }
    }
}
