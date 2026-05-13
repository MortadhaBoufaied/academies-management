package com.footballacademy.services.webhook;

import com.footballacademy.model.Webhook;
import com.footballacademy.model.WebhookLog;
import com.footballacademy.repository.WebhookLogRepository;
import com.footballacademy.repository.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.
class)
class WebhookServiceTest {
    @Mock
    private WebhookRepository webhookRepository;
    @Mock
    private WebhookLogRepository webhookLogRepository;
    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @InjectMocks
    private WebhookService webhookService;
    private Webhook webhook;
    @BeforeEach void setUp() {
        webhook = new Webhook();
        webhook.setId(1L);
        webhook.setName("Test Webhook");
        webhook.setUrl("https://example.com/webhook");
        webhook.setEventType("USER_CREATED");
        webhook.setIsActive(true);
        webhook.setHttpMethod("POST");
    }
    @Test void createWebhook_WhenValid_ShouldReturnCreatedWebhook() {
        when(webhookRepository.existsByName("Test Webhook")) .thenReturn(false);
        when(webhookRepository.save(any(Webhook.
        class))) .thenReturn(webhook);
        Webhook result = webhookService.createWebhook(webhook);
        assertNotNull(result);
        assertEquals("Test Webhook", result.getName());
        verify(webhookRepository, times(1)) .existsByName("Test Webhook");
        verify(webhookRepository, times(1)) .save(webhook);
    }
    @Test void createWebhook_WhenNameExists_ShouldThrowException() {
        when(webhookRepository.existsByName("Test Webhook")) .thenReturn(true);
        assertThrows(IllegalArgumentException.
        class,() -> {
            webhookService.createWebhook(webhook);
        });
        verify(webhookRepository, times(1)) .existsByName("Test Webhook");
        verify(webhookRepository, never()) .save(any(Webhook.
        class));
    }
    @Test void updateWebhook_WhenValid_ShouldReturnUpdatedWebhook() {
        Webhook updatedWebhook = new Webhook();
        updatedWebhook.setName("Test Webhook");
        updatedWebhook.setUrl("https://example.com/updated");
        updatedWebhook.setEventType("PAYMENT_COMPLETED");
        updatedWebhook.setIsActive(true);
        updatedWebhook.setHttpMethod("POST");
        when(webhookRepository.findById(1L)) .thenReturn(Optional.of(webhook));
        when(webhookRepository.existsByName("Test Webhook")) .thenReturn(true);
        when(webhookRepository.save(any(Webhook.
        class))) .thenReturn(webhook);
        Webhook result = webhookService.updateWebhook(1L, updatedWebhook);
        assertNotNull(result);
        verify(webhookRepository, times(1)) .findById(1L);
        verify(webhookRepository, times(1)) .save(any(Webhook.
        class));
    }
    @Test void deleteWebhook_WhenExists_ShouldDeleteWebhook() {
        when(webhookRepository.existsById(1L)) .thenReturn(true);
        doNothing() .when(webhookRepository) .deleteById(1L);
        webhookService.deleteWebhook(1L);
        verify(webhookRepository, times(1)) .existsById(1L);
        verify(webhookRepository, times(1)) .deleteById(1L);
    }
    @Test void deleteWebhook_WhenNotExists_ShouldThrowException() {
        when(webhookRepository.existsById(999L)) .thenReturn(false);
        assertThrows(IllegalArgumentException.
        class,() -> {
            webhookService.deleteWebhook(999L);
        });
        verify(webhookRepository, times(1)) .existsById(999L);
        verify(webhookRepository, never()) .deleteById(anyLong());
    }
    @Test void getWebhookById_WhenExists_ShouldReturnWebhook() {
        when(webhookRepository.findById(1L)) .thenReturn(Optional.of(webhook));
        Webhook result = webhookService.getWebhookById(1L);
        assertNotNull(result);
        assertEquals(webhook, result);
        verify(webhookRepository, times(1)) .findById(1L);
    }
    @Test void getWebhookById_WhenNotExists_ShouldThrowException() {
        when(webhookRepository.findById(999L)) .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.
        class,() -> {
            webhookService.getWebhookById(999L);
        });
        verify(webhookRepository, times(1)) .findById(999L);
    }
    @Test void getActiveWebhooks_ShouldReturnActiveWebhooks() {
        Webhook activeWebhook = new Webhook();
        activeWebhook.setId(1L);
        activeWebhook.setIsActive(true);
        Webhook inactiveWebhook = new Webhook();
        inactiveWebhook.setId(2L);
        inactiveWebhook.setIsActive(false);
        when(webhookRepository.findByIsActiveTrue()) .thenReturn(Arrays.asList(activeWebhook));
        List<Webhook> result = webhookService.getActiveWebhooks();
        assertEquals(1, result.size());
        assertTrue(result.contains(activeWebhook));
        assertFalse(result.contains(inactiveWebhook));
        verify(webhookRepository, times(1)) .findByIsActiveTrue();
    }
    @Test void getWebhooksByEventType_ShouldReturnWebhooksForEventType() {
        Webhook userCreatedWebhook = new Webhook();
        userCreatedWebhook.setId(1L);
        userCreatedWebhook.setEventType("USER_CREATED");
        Webhook paymentWebhook = new Webhook();
        paymentWebhook.setId(2L);
        paymentWebhook.setEventType("PAYMENT_COMPLETED");
        when(webhookRepository.findByEventType("USER_CREATED")) .thenReturn(Arrays.asList(userCreatedWebhook));
        List<Webhook> result = webhookService.getWebhooksByEventType("USER_CREATED");
        assertEquals(1, result.size());
        assertTrue(result.contains(userCreatedWebhook));
        assertFalse(result.contains(paymentWebhook));
        verify(webhookRepository, times(1)) .findByEventType("USER_CREATED");
    }
    @Test void getWebhookLogs_ShouldReturnLogsForWebhook() {
        WebhookLog log1 = new WebhookLog();
        log1.setId(1L);
        WebhookLog log2 = new WebhookLog();
        log2.setId(2L);
        when(webhookLogRepository.findByWebhookIdOrderByExecutedAtDesc(1L)) .thenReturn(Arrays.asList(log1, log2));
        List<WebhookLog> result = webhookService.getWebhookLogs(1L);
        assertEquals(2, result.size());
        assertTrue(result.contains(log1));
        assertTrue(result.contains(log2));
        verify(webhookLogRepository, times(1)) .findByWebhookIdOrderByExecutedAtDesc(1L);
    }
    @Test void getFailedWebhookLogs_ShouldReturnFailedLogs() {
        WebhookLog failedLog = new WebhookLog();
        failedLog.setId(1L);
        failedLog.setSuccess(false);
        WebhookLog successLog = new WebhookLog();
        successLog.setId(2L);
        successLog.setSuccess(true);
        when(webhookLogRepository.findBySuccessFalseOrderByExecutedAtDesc()) .thenReturn(Arrays.asList(failedLog));
        List<WebhookLog> result = webhookService.getFailedWebhookLogs();
        assertEquals(1, result.size());
        assertTrue(result.contains(failedLog));
        assertFalse(result.contains(successLog));
        verify(webhookLogRepository, times(1)) .findBySuccessFalseOrderByExecutedAtDesc();
    }
    @Test void activateWebhook_ShouldSetIsActiveToTrue() {
        webhook.setIsActive(false);
        when(webhookRepository.findById(1L)) .thenReturn(Optional.of(webhook));
        when(webhookRepository.save(any(Webhook.
        class))) .thenReturn(webhook);
        Webhook result = webhookService.activateWebhook(1L);
        assertTrue(result.getIsActive());
        verify(webhookRepository, times(1)) .findById(1L);
        verify(webhookRepository, times(1)) .save(webhook);
    }
    @Test void deactivateWebhook_ShouldSetIsActiveToFalse() {
        webhook.setIsActive(true);
        when(webhookRepository.findById(1L)) .thenReturn(Optional.of(webhook));
        when(webhookRepository.save(any(Webhook.
        class))) .thenReturn(webhook);
        Webhook result = webhookService.deactivateWebhook(1L);
        assertFalse(result.getIsActive());
        verify(webhookRepository, times(1)) .findById(1L);
        verify(webhookRepository, times(1)) .save(webhook);
    }
}
