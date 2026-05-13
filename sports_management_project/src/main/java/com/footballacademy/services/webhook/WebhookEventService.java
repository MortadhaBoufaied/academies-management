package com.footballacademy.services.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public
class WebhookEventService {
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;
    public WebhookEventService(WebhookService webhookService, ObjectMapper objectMapper) {
        this.webhookService = webhookService;
        this.objectMapper = objectMapper;
    }
    /**      * Trigger webhook for user creation      */
    public void triggerUserCreated(Long userId, String email, String role) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "USER_CREATED");
        payload.put("userId", userId);
        payload.put("email", email);
        payload.put("role", role);
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks("USER_CREATED", payload);
    }
    /**      * Trigger webhook for payment completion      */
    public void triggerPaymentCompleted(Long paymentId, Long playerId, Double amount, String currency) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "PAYMENT_COMPLETED");
        payload.put("paymentId", paymentId);
        payload.put("playerId", playerId);
        payload.put("amount", amount);
        payload.put("currency", currency);
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks("PAYMENT_COMPLETED", payload);
    }
    /**      * Trigger webhook for activity creation      */
    public void triggerActivityCreated(Long activityId, String title, LocalDateTime date, Long divisionId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "ACTIVITY_CREATED");
        payload.put("activityId", activityId);
        payload.put("title", title);
        payload.put("date", date.toString());
        payload.put("divisionId", divisionId);
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks("ACTIVITY_CREATED", payload);
    }
    /**      * Trigger webhook for player registration      */
    public void triggerPlayerRegistered(Long playerId, String playerName, Long divisionId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "PLAYER_REGISTERED");
        payload.put("playerId", playerId);
        payload.put("playerName", playerName);
        payload.put("divisionId", divisionId);
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks("PLAYER_REGISTERED", payload);
    }
    /**      * Trigger webhook for match result      */
    public void triggerMatchResult(Long matchId, String homeTeam, String awayTeam, Integer homeScore, Integer awayScore) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "MATCH_RESULT");
        payload.put("matchId", matchId);
        payload.put("homeTeam", homeTeam);
        payload.put("awayTeam", awayTeam);
        payload.put("homeScore", homeScore);
        payload.put("awayScore", awayScore);
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks("MATCH_RESULT", payload);
    }
    /**      * Trigger webhook for notification sent      */
    public void triggerNotificationSent(Long notificationId, Long userId, String type, String title) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "NOTIFICATION_SENT");
        payload.put("notificationId", notificationId);
        payload.put("userId", userId);
        payload.put("type", type);
        payload.put("title", title);
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks("NOTIFICATION_SENT", payload);
    }
    /**      * Trigger webhook for training session completed      */
    public void triggerTrainingCompleted(Long trainingId, Long divisionId, LocalDateTime date) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "TRAINING_COMPLETED");
        payload.put("trainingId", trainingId);
        payload.put("divisionId", divisionId);
        payload.put("date", date.toString());
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks("TRAINING_COMPLETED", payload);
    }
    /**      * Trigger webhook for player statistics updated      */
    public void triggerPlayerStatsUpdated(Long playerId, Integer goals, Integer assists, Double rating) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "PLAYER_STATS_UPDATED");
        payload.put("playerId", playerId);
        payload.put("goals", goals);
        payload.put("assists", assists);
        payload.put("rating", rating);
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks("PLAYER_STATS_UPDATED", payload);
    }
    /**      * Trigger webhook for system alert      */
    public void triggerSystemAlert(String alertType, String message, String severity) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "SYSTEM_ALERT");
        payload.put("alertType", alertType);
        payload.put("message", message);
        payload.put("severity", severity);
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks("SYSTEM_ALERT", payload);
    }
    /**      * Trigger custom webhook event      */
    public void triggerCustomEvent(String eventType, Map<String, Object> customPayload) {
        Map<String, Object> payload = new HashMap<>(customPayload);
        payload.put("eventType", eventType);
        payload.put("timestamp", LocalDateTime.now() .toString());
        webhookService.triggerWebhooks(eventType, payload);
    }
}
