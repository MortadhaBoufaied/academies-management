package com.footballacademy.config.scheduler;

import com.footballacademy.DTO.SendNotificationRequest;
import com.footballacademy.model.Notification;
import com.footballacademy.repository.NotificationRepository;
import com.footballacademy.services.NotificationService;
import com.footballacademy.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.logging.Logger;

@Component
public
class ScheduledTasks {
    private static final Logger logger = Logger.getLogger(ScheduledTasks.
    class.getName());
    private static final Long SYSTEM_ADMIN_ID = 1L;
    // System admin ID for scheduled notifications
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationRepository notificationRepository;
    /**      * Run at 00:00 on the first day of each month      * Resets monthly payment status and sends notifications to parents      */
    @Scheduled(cron = "0 0 0 1 * *", zone = "Europe/Paris")
    public void monthlyPaymentReset() {
        logger.info("=== Monthly Payment Reset Started ===");
        try {
            // 1. Reset payment status for all players
            paymentService.resetMonthlyPayments();
            logger.info("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ Payment status reset for all players");
            // 2. Create payment records for current month (if not exist)
            LocalDate today = LocalDate.now();
            // Assuming default payment amount is 100 (adjust as needed)
            paymentService.createMonthlyPaymentsForAllPlayers(today, 100.0);
            logger.info("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ Payment records created for current month");
            // 3. Send payment reminder notifications to parents             sendMonthlyPaymentReminder();
            logger.info("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ Payment reminders sent to parents");
            logger.info("=== Monthly Payment Reset Completed Successfully ===");
        } catch (Exception e) {
            logger.severe("Error during monthly payment reset: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**      * Send payment reminder to all parents at the start of the month      */
    private void sendMonthlyPaymentReminder() {
        LocalDate today = LocalDate.now();
        String monthYear = today.getMonth() .toString() + " " + today.getYear();
        String title = "Payment Reminder - " + monthYear;
        String content = "Dear Parent,\n\n" + "Welcome to the new month! This is a reminder that monthly fees for your children are now due.\n\n" + "Please visit the academy or contact us to process your payment.\n\n" + "If you have already paid, please disregard this message.\n\n" + "Best regards,\nFootball Academy Administration";
        // We'll send to all parents - implementation in NotificationService
        SendNotificationRequest request = new SendNotificationRequest(title, content, Notification.Category.PARENTS, null, true);
        notificationService.sendNotification(request, SYSTEM_ADMIN_ID);
        logger.info("Payment reminder notifications sent to all parents");
    }
    /**      * Optional: Send a reminder at mid-month for unpaid payments      * Run at 09:00 on the 15th of each month      */
    @Scheduled(cron = "0 0 9 15 * *", zone = "Europe/Paris")
    public void midMonthPaymentReminder() {
        logger.info("=== Mid-Month Payment Reminder Started ===");
        try {
            var unpaidParents = paymentService.getParentsWhoDidntPayThisMonth();
            if (unpaidParents.isEmpty()) {
                logger.info("All parents have paid - no reminder needed");
                return;
            }
            String title = "Payment Reminder - Action Required";
            String content = "Dear Parent,\n\n" + "We noticed that your payment for this month is still pending.\n\n" + "Please log in to your academy portal to view the details and process payment.\n\n" + "If payment has already been made, please disregard this message.\n\n" + "Need help? Contact us at the academy.\n\n" + "Best regards,\nFootball Academy Administration";
            SendNotificationRequest request = new SendNotificationRequest(title, content, Notification.Category.PARENTS, unpaidParents.stream() .map(p -> p.getUser() .getId()) .collect(java.util.stream.Collectors.toList()), false);
            notificationService.sendNotification(request, SYSTEM_ADMIN_ID);
            logger.info("Mid-month payment reminders sent to " + unpaidParents.size() + " parents");
        } catch (Exception e) {
            logger.severe("Error during mid-month payment reminder: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**      * Process and send scheduled notifications that are due      * Run every hour at minute 0      */
    @Scheduled(cron = "0 0 * * * *", zone = "Europe/Paris")
    public void processScheduledNotifications() {
        try {
            var scheduledNotifications = notificationRepository.findByIsScheduledTrueAndScheduledForBefore(java.time.LocalDateTime.now());
            if (scheduledNotifications.isEmpty()) {
                return;
            }
            for (Notification notification : scheduledNotifications) {
                // Mark as no longer scheduled (already sent)
                notification.setScheduled(false);
                notificationRepository.save(notification);
                logger.info("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ Scheduled notification processed: " + notification.getTitle());
            }
        } catch (Exception e) {
            logger.severe("Error processing scheduled notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**      * Clean up old read notifications (older than 90 days)      * Run at 02:00 every Sunday      */
    @Scheduled(cron = "0 0 2 * * SUN", zone = "Europe/Paris")
    public void cleanupOldNotifications() {
        logger.info("=== Cleanup Old Notifications Started ===");
        try {
            java.time.LocalDateTime ninetyDaysAgo = java.time.LocalDateTime.now() .minusDays(90);
            var allNotifications = notificationRepository.findAll();
            var toDelete = allNotifications.stream() .filter(n -> n.isRead() && n.getCreatedAt() .isBefore(ninetyDaysAgo)) .collect(java.util.stream.Collectors.toList());
            if (!toDelete.isEmpty()) {
                notificationRepository.deleteAll(toDelete);
                logger.info("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ Deleted " + toDelete.size() + " old read notifications");
            } else {
                logger.info("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ No old notifications to delete");
            }
        } catch (Exception e) {
            logger.severe("Error cleaning up old notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
