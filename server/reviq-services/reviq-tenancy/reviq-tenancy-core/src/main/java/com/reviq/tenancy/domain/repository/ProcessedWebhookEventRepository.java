package com.reviq.tenancy.domain.repository;

import com.reviq.tenancy.domain.entity.ProcessedWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ProcessedWebhookEventRepository extends JpaRepository<ProcessedWebhookEvent, UUID> {

    boolean existsByEventId(String eventId);

    @Modifying
    @Query("DELETE FROM ProcessedWebhookEvent e WHERE e.processedAt < :before")
    void deleteEventsOlderThan(LocalDateTime before);
}
