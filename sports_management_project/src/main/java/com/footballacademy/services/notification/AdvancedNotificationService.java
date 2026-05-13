package com.footballacademy.services.notification;

import com.footballacademy.model.Notification;
import com.footballacademy.model.User;
import com.footballacademy.repository.ActivityRepository;
import com.footballacademy.repository.NotificationRepository;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public
class AdvancedNotificationService {
    private static final Logger log = LoggerFactory.getLogger(AdvancedNotificationService.
    class);
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final PaymentRepository paymentRepository;
    /**      * Send custom notification to a specific user      */
    @Transactional
    public Notification sendCustomNotification(Long userId, String title, String message, String type) {
        User user = userRepository.findById(userId) .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(message);
        notification.setContentHtml(message);
        notification.setCategory(Notification.Category.valueOf(type.toUpperCase()));
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setCreatedBy(1L);
        notification.setAcademy(user.getAcademy());
        Notification saved = notificationRepository.save(notification);
        log.info("Custom notification sent to user {}: {}", userId, title);
        return saved;
    }
    /**      * Send broadcast notification to all users      */
    @Transactional
    public List<Notification> sendBroadcastNotification(String title, String message, String type) {
        List<User> allUsers = userRepository.findAll();
        List<Notification> notifications = allUsers.stream() .map(user -> {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setTitle(title);
            notification.setContent(message);
            notification.setContentHtml(message);
            notification.setCategory(Notification.Category.valueOf(type.toUpperCase()));
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setCreatedBy(1L);
            notification.setAcademy(user.getAcademy());
            return notification;
        }) .collect(Collectors.toList());
        List<Notification> saved = notificationRepository.saveAll(notifications);
        log.info("Broadcast notification sent to {} users", saved.size());
        return saved;
    }
    /**      * Send notification to users with specific role      */
    @Transactional
    public List<Notification> sendNotificationByRole(String role, String title, String message, String type) {
        List<User> usersWithRole = userRepository.findByMainRole(User.UserRole.valueOf(role));
        List<Notification> notifications = usersWithRole.stream() .map(user -> {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setTitle(title);
            notification.setContent(message);
            notification.setContentHtml(message);
            notification.setCategory(Notification.Category.valueOf(type.toUpperCase()));
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setCreatedBy(1L);
            notification.setAcademy(user.getAcademy());
            return notification;
        }) .collect(Collectors.toList());
        List<Notification> saved = notificationRepository.saveAll(notifications);
        log.info("Role-based notification sent to {} users with role {}", saved.size(), role);
        return saved;
    }
    /**      * Mark notification as read      */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId) .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + notificationId));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("Notification {} marked as read", notificationId);
    }
    /**      * Mark all notifications as read for a user      */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
    }
    /**      * Get unread notifications for a user      */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    /**      * Get all notifications for a user      */
    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    /**      * Scheduled task: Send activity reminders 24 hours before      */
    @Scheduled(cron = "0 0 9 * * ?")
    // Run daily at 9 AM
    @Transactional
    public void sendActivityReminders24Hours() {
        LocalDateTime tomorrow = LocalDateTime.now() .plusDays(1);
        LocalDateTime startOfDay = tomorrow.toLocalDate() .atStartOfDay();
        LocalDateTime endOfDay = tomorrow.toLocalDate() .atTime(23, 59, 59);
        List<com.footballacademy.model.Activity> activitiesTomorrow = activityRepository .findByDateBetween(startOfDay, endOfDay);
        activitiesTomorrow.forEach(activity -> {
            String title = "Activity Reminder: " + activity.getTitre();
            String message = "You have an activity scheduled for tomorrow: " + activity.getTitre();
            sendBroadcastNotification(title, message, "GENERAL");
        });
        log.info("Sent 24-hour activity reminders for {} activities", activitiesTomorrow.size());
    }
    /**      * Scheduled task: Send activity reminders 2 hours before      */
    @Scheduled(cron = "0 0 * * * ?")
    // Run every hour
    @Transactional
    public void sendActivityReminders2Hours() {
        LocalDateTime twoHoursLater = LocalDateTime.now() .plusHours(2);
        LocalDateTime oneHourLater = LocalDateTime.now() .plusHours(1);
        List<com.footballacademy.model.Activity> upcomingActivities = activityRepository .findByDateBetween(oneHourLater, twoHoursLater);
        upcomingActivities.forEach(activity -> {
            String title = "Activity Starting Soon: " + activity.getTitre();
            String message = "Your activity '" + activity.getTitre() + "' starts in 2 hours";
            sendBroadcastNotification(title, message, "GENERAL");
        });
        log.info("Sent 2-hour activity reminders for {} activities", upcomingActivities.size());
    }
    /**      * Scheduled task: Send payment due reminders 3 days before      */
    @Scheduled(cron = "0 0 8 * * ?")
    // Run daily at 8 AM
    @Transactional
    public void sendPaymentDueReminders3Days() {
        LocalDateTime threeDaysLater = LocalDateTime.now() .plusDays(3);
        LocalDateTime twoDaysLater = LocalDateTime.now() .plusDays(2);
        List<com.footballacademy.model.Payment> duePayments = paymentRepository .findByDueDateBetweenAndIsPaidFalse(twoDaysLater, threeDaysLater);
        duePayments.forEach(payment -> {
            if (payment.getParent() != null) {
                String title = "Payment Due Soon";
                String message = "Your payment of " + payment.getAmount() + " " + payment.getCurrency() + " is due in 3 days";
                sendCustomNotification(payment.getParent() .getId(), title, message, "GENERAL");
            }
        });
        log.info("Sent 3-day payment due reminders for {} payments", duePayments.size());
    }
    /**      * Scheduled task: Send payment due reminders 1 day before      */
    @Scheduled(cron = "0 0 8 * * ?")
    // Run daily at 8 AM
    @Transactional
    public void sendPaymentDueReminders1Day() {
        LocalDateTime tomorrow = LocalDateTime.now() .plusDays(1);
        LocalDateTime today = LocalDateTime.now();
        List<com.footballacademy.model.Payment> duePayments = paymentRepository .findByDueDateBetweenAndIsPaidFalse(today, tomorrow);
        duePayments.forEach(payment -> {
            if (payment.getParent() != null) {
                String title = "Payment Due Tomorrow";
                String message = "Your payment of " + payment.getAmount() + " " + payment.getCurrency() + " is due tomorrow";
                sendCustomNotification(payment.getParent() .getId(), title, message, "GENERAL");
            }
        });
        log.info("Sent 1-day payment due reminders for {} payments", duePayments.size());
    }
    /**      * Scheduled task: Send overdue payment alerts      */
    @Scheduled(cron = "0 0 9 * * ?")
    // Run daily at 9 AM
    @Transactional
    public void sendOverduePaymentAlerts() {
        LocalDateTime now = LocalDateTime.now();
        // 7 days overdue
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        List<com.footballacademy.model.Payment> overdue7Days = paymentRepository .findByDueDateBeforeAndIsPaidFalse(sevenDaysAgo);
        overdue7Days.forEach(payment -> {
            if (payment.getParent() != null) {
                String title = "Payment Overdue - 7 Days";
                String message = "Your payment of " + payment.getAmount() + " " + payment.getCurrency() + " is 7 days overdue";
                sendCustomNotification(payment.getParent() .getId(), title, message, "GENERAL");
            }
        });
        // 14 days overdue
        LocalDateTime fourteenDaysAgo = now.minusDays(14);
        List<com.footballacademy.model.Payment> overdue14Days = paymentRepository .findByDueDateBeforeAndIsPaidFalse(fourteenDaysAgo);
        overdue14Days.forEach(payment -> {
            if (payment.getParent() != null) {
                String title = "Payment Overdue - 14 Days";
                String message = "Your payment of " + payment.getAmount() + " " + payment.getCurrency() + " is 14 days overdue. Please pay immediately.";
                sendCustomNotification(payment.getParent() .getId(), title, message, "GENERAL");
            }
        });
        // 30 days overdue
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        List<com.footballacademy.model.Payment> overdue30Days = paymentRepository .findByDueDateBeforeAndIsPaidFalse(thirtyDaysAgo);
        overdue30Days.forEach(payment -> {
            if (payment.getParent() != null) {
                String title = "Payment Overdue - 30 Days";
                String message = "Your payment of " + payment.getAmount() + " " + payment.getCurrency() + " is 30 days overdue. Urgent action required.";
                sendCustomNotification(payment.getParent() .getId(), title, message, "GENERAL");
            }
        });
        log.info("Sent overdue payment alerts: 7 days={}, 14 days={}, 30 days={}", overdue7Days.size(), overdue14Days.size(), overdue30Days.size());
    }
    /**      * Get notification statistics      */
    public Map<String, Object> getNotificationStatistics(Long userId) {
        List<Notification> allNotifications = getAllNotifications(userId);
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        Map<String, Object> stats = Map.of("total", allNotifications.size(), "unread", unreadNotifications.size(), "read", allNotifications.size() - unreadNotifications.size(), "unreadPercentage", allNotifications.size() > 0 ?(unreadNotifications.size() * 100.0 / allNotifications.size()) : 0.0);
        return stats;
    }
}
