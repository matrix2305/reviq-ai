package com.reviq.tenancy.application.service;

import com.reviq.tenancy.domain.repository.ProcessedWebhookEventRepository;
import com.reviq.tenancy.domain.repository.TenantSubscriptionRepository;
import com.reviq.tenancy.shared.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionMaintenanceService {

    private final TenantSubscriptionRepository subscriptionRepository;
    private final ProcessedWebhookEventRepository webhookEventRepository;

    @Value("${subscription.grace-period-days:7}")
    private int gracePeriodDays;

    @Value("${webhook.cleanup.retention-days:30}")
    private int webhookRetentionDays;

    @Scheduled(cron = "${subscription.trial-expiration.cron:0 0 2 * * *}")
    @SchedulerLock(name = "processExpiredTrials", lockAtLeastFor = "5m", lockAtMostFor = "1h")
    @Transactional
    public void processExpiredTrials() {
        var expired = subscriptionRepository.findByStatusAndTrialEndDateBefore(
                SubscriptionStatus.TRIAL, LocalDate.now());

        for (var sub : expired) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
            log.info("Trial expired for tenant: {}", sub.getTenant().getCode());
        }

        if (!expired.isEmpty()) {
            log.info("Processed {} expired trials", expired.size());
        }
    }

    @Scheduled(cron = "${subscription.past-due-expiration.cron:0 0 3 * * *}")
    @SchedulerLock(name = "processPastDueExpired", lockAtLeastFor = "5m", lockAtMostFor = "1h")
    @Transactional
    public void processPastDueToExpired() {
        LocalDate cutoff = LocalDate.now().minusDays(gracePeriodDays);
        var pastDue = subscriptionRepository.findByStatusAndEndDateBefore(
                SubscriptionStatus.PAST_DUE, cutoff);

        for (var sub : pastDue) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
            log.info("Past due subscription expired for tenant: {}", sub.getTenant().getCode());
        }

        if (!pastDue.isEmpty()) {
            log.info("Processed {} past due → expired subscriptions", pastDue.size());
        }
    }

    @Scheduled(cron = "${webhook.cleanup.cron:0 0 5 * * *}")
    @SchedulerLock(name = "cleanupWebhookEvents", lockAtLeastFor = "5m", lockAtMostFor = "30m")
    @Transactional
    public void cleanupOldWebhookEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(webhookRetentionDays);
        webhookEventRepository.deleteEventsOlderThan(cutoff);
        log.info("Cleaned up webhook events older than {} days", webhookRetentionDays);
    }
}
