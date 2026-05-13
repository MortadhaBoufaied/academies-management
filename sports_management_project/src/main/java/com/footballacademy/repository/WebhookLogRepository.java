package com.footballacademy.repository;

import com.footballacademy.model.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public
interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
    List<WebhookLog> findByWebhookIdOrderByExecutedAtDesc(Long webhookId);
    List<WebhookLog> findByEventTypeOrderByExecutedAtDesc(String eventType);
    List<WebhookLog> findBySuccessFalseOrderByExecutedAtDesc();
    List<WebhookLog> findTop10ByOrderByExecutedAtDesc();
    List<WebhookLog> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByWebhookIdAndSuccess(Long webhookId, Boolean success);
}
