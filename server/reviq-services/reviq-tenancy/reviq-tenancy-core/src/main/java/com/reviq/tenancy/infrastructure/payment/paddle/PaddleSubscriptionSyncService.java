package com.reviq.tenancy.infrastructure.payment.paddle;

import com.reviq.tenancy.domain.entity.TenantSubscription;
import com.reviq.tenancy.domain.repository.TenantRepository;
import com.reviq.tenancy.domain.repository.TenantSubscriptionRepository;
import com.reviq.tenancy.infrastructure.payment.paddle.dto.PaddleWebhookEvent;
import com.reviq.tenancy.shared.enums.BillingCycle;
import com.reviq.tenancy.shared.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaddleSubscriptionSyncService {

    private final TenantSubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;

    public void syncSubscriptionFromWebhook(Map<String, Object> data) {
        String paddleSubId = (String) data.get("id");
        var subscription = subscriptionRepository.findByPaddleSubscriptionId(paddleSubId).orElse(null);

        if (subscription == null) {
            UUID tenantId = extractTenantId(data);
            if (tenantId == null) return;
            subscription = subscriptionRepository.findByTenantId(tenantId).orElse(null);
        }

        if (subscription == null) {
            log.warn("No subscription found for Paddle subscription: {}", paddleSubId);
            return;
        }

        subscription.setPaddleSubscriptionId(paddleSubId);
        String customerId = (String) data.get("customer_id");
        if (customerId != null) subscription.setPaddleCustomerId(customerId);

        String status = (String) data.get("status");
        if (status != null) subscription.setStatus(PaddleStatusMapper.toSubscriptionStatus(status));

        updateDates(subscription, data);
        updateBillingCycle(subscription, data);
        subscriptionRepository.save(subscription);

        log.info("Synced subscription for tenant from Paddle: {}", paddleSubId);
    }

    public void activateSubscription(Map<String, Object> data) {
        String paddleSubId = (String) data.get("id");
        var subscription = subscriptionRepository.findByPaddleSubscriptionId(paddleSubId)
                .orElseGet(() -> {
                    UUID tenantId = extractTenantId(data);
                    return tenantId != null ? subscriptionRepository.findByTenantId(tenantId).orElse(null) : null;
                });

        if (subscription == null) {
            log.warn("No subscription to activate for: {}", paddleSubId);
            return;
        }

        subscription.setPaddleSubscriptionId(paddleSubId);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        updateDates(subscription, data);
        subscriptionRepository.save(subscription);

        log.info("Activated subscription: {}", paddleSubId);
    }

    public void cancelSubscription(Map<String, Object> data) {
        String paddleSubId = (String) data.get("id");
        var subscription = subscriptionRepository.findByPaddleSubscriptionId(paddleSubId).orElse(null);
        if (subscription == null) return;

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setCanceledAt(LocalDateTime.now());
        subscription.setAutoRenew(false);

        Map<String, Object> scheduledChange = (Map<String, Object>) data.get("scheduled_change");
        if (scheduledChange != null && scheduledChange.get("effective_at") != null) {
            subscription.setAccessEndsAt(LocalDateTime.parse((String) scheduledChange.get("effective_at")));
        }

        subscriptionRepository.save(subscription);
        log.info("Canceled subscription: {}", paddleSubId);
    }

    public void markPastDue(Map<String, Object> data) {
        String paddleSubId = (String) data.get("id");
        subscriptionRepository.findByPaddleSubscriptionId(paddleSubId).ifPresent(sub -> {
            sub.setStatus(SubscriptionStatus.PAST_DUE);
            subscriptionRepository.save(sub);
            log.warn("Subscription past due: {}", paddleSubId);
        });
    }

    public void handlePaymentCompleted(Map<String, Object> data) {
        String subscriptionId = (String) data.get("subscription_id");
        if (subscriptionId == null) return;

        subscriptionRepository.findByPaddleSubscriptionId(subscriptionId).ifPresent(sub -> {
            if (sub.getStatus() == SubscriptionStatus.PENDING
                    || sub.getStatus() == SubscriptionStatus.TRIAL
                    || sub.getStatus() == SubscriptionStatus.PAST_DUE) {
                sub.setStatus(SubscriptionStatus.ACTIVE);
                sub.setTrialEndDate(null);
                subscriptionRepository.save(sub);
                log.info("Payment completed, subscription activated: {}", subscriptionId);
            }
        });
    }

    private UUID extractTenantId(Map<String, Object> data) {
        try {
            Map<String, Object> customData = (Map<String, Object>) data.get("custom_data");
            if (customData != null && customData.containsKey("tenant_id")) {
                return UUID.fromString((String) customData.get("tenant_id"));
            }
        } catch (Exception e) {
            log.error("Failed to extract tenant_id from custom_data: {}", e.getMessage());
        }
        return null;
    }

    private void updateDates(TenantSubscription sub, Map<String, Object> data) {
        String startedAt = (String) data.get("started_at");
        if (startedAt != null) sub.setStartDate(LocalDate.parse(startedAt.substring(0, 10)));

        String nextBilledAt = (String) data.get("next_billed_at");
        if (nextBilledAt != null) sub.setNextBillingDate(LocalDate.parse(nextBilledAt.substring(0, 10)));

        Map<String, Object> period = (Map<String, Object>) data.get("current_billing_period");
        if (period != null) {
            String endsAt = (String) period.get("ends_at");
            if (endsAt != null) sub.setEndDate(LocalDate.parse(endsAt.substring(0, 10)));
        }
    }

    private void updateBillingCycle(TenantSubscription sub, Map<String, Object> data) {
        var items = (java.util.List<Map<String, Object>>) data.get("items");
        if (items != null && !items.isEmpty()) {
            var price = (Map<String, Object>) items.get(0).get("price");
            if (price != null) {
                var billingCycle = (Map<String, Object>) price.get("billing_cycle");
                if (billingCycle != null) {
                    String interval = (String) billingCycle.get("interval");
                    sub.setBillingCycle("year".equals(interval) ? BillingCycle.YEARLY : BillingCycle.MONTHLY);
                }
            }
        }
    }
}
