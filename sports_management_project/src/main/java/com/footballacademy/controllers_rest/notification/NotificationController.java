package com.footballacademy.controllers_rest.notification;

import com.footballacademy.DTO.notification.NotificationViewDto;
import com.footballacademy.model.Notification;
import com.footballacademy.model.User;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.NotificationService;
import com.footballacademy.services.academy.AcademyAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public
class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AcademyAccessService academyAccessService;
    private Long resolveUserId(Long requestedUserId) {
        if (requestedUserId != null && requestedUserId > 0) {
            return requestedUserId;
        } Authentication auth = SecurityContextHolder.getContext() .getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName() .isBlank()) {
            return null;
        } return userRepository.findByEmail(auth.getName()) .map(User::getId) .orElse(null);
    }
    private Long resolveCurrentActorId() {
        Long uid = resolveUserId(null);
        return uid != null ? uid : 1L;
    }
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
    @GetMapping
    public ResponseEntity<?> getNotifications(
    @RequestParam(required = false) Long userId) {
        try {
            Long resolvedUserId = resolveUserId(userId);
            if (resolvedUserId == null || resolvedUserId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid user ID is required"));
            } Optional<User> optionalUser = userRepository.findById(resolvedUserId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "User not found with ID: " + resolvedUserId));
            } academyAccessService.assertCanAccessUser(optionalUser.get());
            List<NotificationViewDto> notifications = notificationService.getNotificationViewsForUser(resolvedUserId);
            if (notifications == null || notifications.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch notifications: " + e.getMessage()));
        }
    }
    @PostMapping
    public ResponseEntity<?> addNotification(
    @RequestParam String title,
    @RequestParam String content,
    @RequestParam Long userId) {
        try {
            if (title == null || title.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Title is required"));
            }
            if (content == null || content.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Content is required"));
            }
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid user ID is required"));
            } Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "User not found with ID: " + userId));
            } academyAccessService.assertCanAccessUser(optionalUser.get());
            Notification notification = notificationService.addNotification(title, content, userId);
            return ResponseEntity.status(HttpStatus.CREATED) .body(NotificationViewDto.from(notification, "System"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to add notification: " + e.getMessage()));
        }
    }
    @GetMapping("/count")
    public ResponseEntity<?> getNotificationsNumber(
    @RequestParam(required = false) Long userId) {
        try {
            Long resolvedUserId = resolveUserId(userId);
            if (resolvedUserId == null || resolvedUserId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid user ID is required"));
            } Optional<User> optionalUser = userRepository.findById(resolvedUserId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "User not found with ID: " + resolvedUserId));
            } academyAccessService.assertCanAccessUser(optionalUser.get());
            long unreadCount = notificationService.countUnreadNotifications(resolvedUserId);
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to count notifications: " + e.getMessage()));
        }
    }
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
    @PathVariable Long notificationId) {
        return markAsReadInternal(notificationId);
    }
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsReadByPut(
    @PathVariable Long notificationId) {
        return markAsReadInternal(notificationId);
    }
    private ResponseEntity<?> markAsReadInternal(Long notificationId) {
        try {
            if (notificationId == null || notificationId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid notification ID is required"));
            } Notification notification = notificationService.markAsRead(notificationId);
            if (notification == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Notification not found with ID: " + notificationId));
            } String senderName = userRepository.findById(notification.getCreatedBy()) .map(user -> user.getNom() != null && !user.getNom() .isBlank() ? user.getNom() : user.getEmail()) .orElse("System");
            return ResponseEntity.ok(NotificationViewDto.from(notification, senderName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to mark notification as read: " + e.getMessage()));
        }
    }
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
    @RequestParam(required = false) Long userId) {
        try {
            Long resolvedUserId = resolveUserId(userId);
            if (resolvedUserId == null || resolvedUserId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid user ID is required"));
            } Optional<User> optionalUser = userRepository.findById(resolvedUserId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "User not found with ID: " + resolvedUserId));
            } academyAccessService.assertCanAccessUser(optionalUser.get());
            int markedCount = notificationService.markAllAsRead(resolvedUserId);
            return ResponseEntity.ok(Map.of("markedCount", markedCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to mark all notifications as read: " + e.getMessage()));
        }
    }
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
    @PathVariable Long notificationId) {
        try {
            if (notificationId == null || notificationId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid notification ID is required"));
            } notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(Map.of("message", "Notification deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to delete notification: " + e.getMessage()));
        }
    }
    @PostMapping("/payment-reminder")
    public ResponseEntity<?> sendPaymentReminder(
    @RequestBody Map<String, Object> body) {
        try {
            Long userId = toLong(body.get("userId"));
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid userId is required"));
            } User target = userRepository.findById(userId) .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            academyAccessService.assertCanAccessUser(target);
            String message = String.valueOf(body.getOrDefault("message", "Payment reminder"));
            Notification notification = new Notification("Payment Reminder", message, Notification.Category.GENERAL, userId, resolveCurrentActorId());
            Notification saved = notificationService.saveNotification(notification);
            return ResponseEntity.status(HttpStatus.CREATED) .body(NotificationViewDto.from(saved, "System"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to send payment reminder: " + e.getMessage()));
        }
    }
    @PostMapping("/activity-reminder")
    public ResponseEntity<?> sendActivityReminder(
    @RequestBody Map<String, Object> body) {
        try {
            Long userId = toLong(body.get("userId"));
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid userId is required"));
            } User target = userRepository.findById(userId) .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            academyAccessService.assertCanAccessUser(target);
            String title = String.valueOf(body.getOrDefault("activityTitle", "Activity"));
            String date = String.valueOf(body.getOrDefault("date", ""));
            String content = date.isBlank() ? "Reminder: " + title : "Reminder: " + title + " on " + date;
            Notification notification = new Notification("Activity Reminder", content, Notification.Category.GENERAL, userId, resolveCurrentActorId());
            Notification saved = notificationService.saveNotification(notification);
            return ResponseEntity.status(HttpStatus.CREATED) .body(NotificationViewDto.from(saved, "System"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to send activity reminder: " + e.getMessage()));
        }
    }
}
