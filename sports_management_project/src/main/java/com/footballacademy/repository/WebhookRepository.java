package com.footballacademy.repository;

import com.footballacademy.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface WebhookRepository extends JpaRepository<Webhook, Long> {
    List<Webhook> findByActiveTrue();
    List<Webhook> findByEventType(String eventType);
    List<Webhook> findByEventTypeAndActiveTrue(String eventType);
    Optional<Webhook> findByName(String name);
    boolean existsByName(String name);
}
