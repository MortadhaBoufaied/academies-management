package com.footballacademy.controllers_rest.webhook;

import com.footballacademy.model.Webhook;
import com.footballacademy.model.WebhookLog;
import com.footballacademy.services.webhook.WebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public
class WebhookController {
    private final WebhookService webhookService;
    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }
    /**      * Get all webhooks      */
    @GetMapping
    public ResponseEntity<List<Webhook>> getAllWebhooks() {
        return ResponseEntity.ok(webhookService.getAllWebhooks());
    }
    /**      * Get all active webhooks      */
    @GetMapping("/active")
    public ResponseEntity<List<Webhook>> getActiveWebhooks() {
        return ResponseEntity.ok(webhookService.getActiveWebhooks());
    }
    /**      * Get webhooks by event type      */
    @GetMapping("/event-type/{eventType}")
    public ResponseEntity<List<Webhook>> getWebhooksByEventType(
    @PathVariable String eventType) {
        return ResponseEntity.ok(webhookService.getWebhooksByEventType(eventType));
    }
    /**      * Get webhook by ID      */
    @GetMapping("/{id}")
    public ResponseEntity<?> getWebhookById(
    @PathVariable Long id) {
        try {
            return ResponseEntity.ok(webhookService.getWebhookById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound() .build();
        }
    }
    /**      * Create webhook      */
    @PostMapping
    public ResponseEntity<?> createWebhook(
    @RequestBody Webhook webhook) {
        try {
            return ResponseEntity.status(201) .body(webhookService.createWebhook(webhook));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    /**      * Update webhook      */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWebhook(
    @PathVariable Long id,
    @RequestBody Webhook webhook) {
        try {
            return ResponseEntity.ok(webhookService.updateWebhook(id, webhook));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    /**      * Delete webhook      */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWebhook(
    @PathVariable Long id) {
        try {
            webhookService.deleteWebhook(id);
            return ResponseEntity.ok(Map.of("message", "Webhook deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    /**      * Activate webhook      */
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateWebhook(
    @PathVariable Long id) {
        try {
            return ResponseEntity.ok(webhookService.activateWebhook(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    /**      * Deactivate webhook      */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateWebhook(
    @PathVariable Long id) {
        try {
            return ResponseEntity.ok(webhookService.deactivateWebhook(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    /**      * Test webhook      */
    @PostMapping("/{id}/test")
    public ResponseEntity<?> testWebhook(
    @PathVariable Long id) {
        try {
            return ResponseEntity.ok(webhookService.testWebhook(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    /**      * Get webhook logs      */
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<WebhookLog>> getWebhookLogs(
    @PathVariable Long id) {
        return ResponseEntity.ok(webhookService.getWebhookLogs(id));
    }
    /**      * Get failed webhook logs      */
    @GetMapping("/logs/failed")
    public ResponseEntity<List<WebhookLog>> getFailedWebhookLogs() {
        return ResponseEntity.ok(webhookService.getFailedWebhookLogs());
    }
}
