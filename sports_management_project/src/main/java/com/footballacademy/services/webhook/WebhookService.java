package com.footballacademy.services.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballacademy.model.Webhook;
import com.footballacademy.model.WebhookLog;
import com.footballacademy.repository.WebhookLogRepository;
import com.footballacademy.repository.WebhookRepository;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public
class WebhookService {
    private final WebhookRepository webhookRepository;
    private final WebhookLogRepository webhookLogRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    public WebhookService(WebhookRepository webhookRepository, WebhookLogRepository webhookLogRepository, ObjectMapper objectMapper) {
        this.webhookRepository = webhookRepository;
        this.webhookLogRepository = webhookLogRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }
    /**      * Trigger webhooks for a specific event type      */
    @Async
    public void triggerWebhooks(String eventType, Map<String, Object> payload) {
        List<Webhook> activeWebhooks = webhookRepository.findByEventTypeAndActiveTrue(eventType);
        for (Webhook webhook : activeWebhooks) {
            executeWebhook(webhook, eventType, payload);
        }
    }
    /**      * Execute a single webhook      */
    private void executeWebhook(Webhook webhook, String eventType, Map<String, Object> payload) {
        long startTime = System.currentTimeMillis();
        WebhookLog log = new WebhookLog();
        log.setWebhook(webhook);
        log.setEventType(eventType);
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Add custom headers if configured
            if (webhook.getHeaders() != null && !webhook.getHeaders() .isEmpty()) {
                Map<String, String> customHeaders = objectMapper.readValue(webhook.getHeaders(), Map.
                class);
                customHeaders.forEach(headers::add);
            }
            // Add authentication if configured
            if (webhook.getAuthentication() != null && !webhook.getAuthentication() .isEmpty()) {
                headers.set("Authorization", webhook.getAuthentication());
            }
            // Prepare request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
            // Determine HTTP method
            HttpMethod method = HttpMethod.valueOf(webhook.getHttpMethod());
            // Execute request
            ResponseEntity<String> response = restTemplate.exchange(webhook.getUrl(), method, requestEntity, String.
            class);
            // Log success
            log.setPayload(objectMapper.writeValueAsString(payload));
            log.setStatusCode(response.getStatusCodeValue());
            log.setResponseBody(response.getBody());
            log.setSuccess(true);
            log.setResponseTimeMs(System.currentTimeMillis() - startTime);
            // Update webhook trigger count
            webhook.incrementTriggerCount();
            webhookRepository.save(webhook);
        } catch (Exception e) {
            // Log failure
            try {
                log.setPayload(objectMapper.writeValueAsString(payload));
            } catch (Exception jsonEx) {
                log.setPayload("{\"error\": \"Failed to serialize payload\"}");
            } log.setStatusCode(500);
            log.setSuccess(false);
            log.setErrorMessage(e.getMessage());
            log.setResponseTimeMs(System.currentTimeMillis() - startTime);
        } webhookLogRepository.save(log);
    }
    /**      * Create webhook      */
    public Webhook createWebhook(Webhook webhook) {
        if (webhookRepository.existsByName(webhook.getName())) {
            throw new IllegalArgumentException("Webhook with name " + webhook.getName() + " already exists");
        } return webhookRepository.save(webhook);
    }
    /**      * Update webhook      */
    public Webhook updateWebhook(Long id, Webhook webhook) {
        Webhook existingWebhook = webhookRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Webhook not found with id: " + id));
        if (!existingWebhook.getName() .equals(webhook.getName()) && webhookRepository.existsByName(webhook.getName())) {
            throw new IllegalArgumentException("Webhook with name " + webhook.getName() + " already exists");
        }
        existingWebhook.setName(webhook.getName());
        existingWebhook.setUrl(webhook.getUrl());
        existingWebhook.setEventType(webhook.getEventType());
        existingWebhook.setActive(webhook.isActive());
        existingWebhook.setHttpMethod(webhook.getHttpMethod());
        existingWebhook.setHeaders(webhook.getHeaders());
        existingWebhook.setAuthentication(webhook.getAuthentication());
        return webhookRepository.save(existingWebhook);
    }
    /**      * Delete webhook      */
    public void deleteWebhook(Long id) {
        if (!webhookRepository.existsById(id)) {
            throw new IllegalArgumentException("Webhook not found with id: " + id);
        } webhookRepository.deleteById(id);
    }
    /**      * Get webhook by ID      */
    public Webhook getWebhookById(Long id) {
        return webhookRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Webhook not found with id: " + id));
    }
    /**      * Get all webhooks      */
    public List<Webhook> getAllWebhooks() {
        return webhookRepository.findAll();
    }
    /**      * Get all active webhooks      */
    public List<Webhook> getActiveWebhooks() {
        return webhookRepository.findByActiveTrue();
    }
    /**      * Get webhooks by event type      */
    public List<Webhook> getWebhooksByEventType(String eventType) {
        return webhookRepository.findByEventType(eventType);
    }
    /**      * Get webhook logs      */
    public List<WebhookLog> getWebhookLogs(Long webhookId) {
        return webhookLogRepository.findByWebhookIdOrderByExecutedAtDesc(webhookId);
    }
    /**      * Get failed webhook logs      */
    public List<WebhookLog> getFailedWebhookLogs() {
        return webhookLogRepository.findBySuccessFalseOrderByExecutedAtDesc();
    }
    /**      * Test webhook      */
    public Map<String, Object> testWebhook(Long webhookId) {
        Webhook webhook = getWebhookById(webhookId);
        Map<String, Object> testPayload = new HashMap<>();
        testPayload.put("test", true);
        testPayload.put("timestamp", LocalDateTime.now() .toString());
        testPayload.put("message", "This is a test webhook trigger");
        executeWebhook(webhook, "TEST", testPayload);
        return Map.of("message", "Webhook test triggered", "webhookId", webhookId, "webhookName", webhook.getName());
    }
    /**      * Activate webhook      */
    public Webhook activateWebhook(Long id) {
        Webhook webhook = getWebhookById(id);
        webhook.setActive(true);
        return webhookRepository.save(webhook);
    }
    /**      * Deactivate webhook      */
    public Webhook deactivateWebhook(Long id) {
        Webhook webhook = getWebhookById(id);
        webhook.setActive(false);
        return webhookRepository.save(webhook);
    }
}
