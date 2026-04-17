package com.reviq.tenancy.application.service;

import com.reviq.tenancy.domain.entity.SubscriptionPlan;
import com.reviq.tenancy.domain.entity.Tenant;
import com.reviq.tenancy.domain.entity.TenantSubscription;
import com.reviq.tenancy.domain.repository.SubscriptionPlanRepository;
import com.reviq.tenancy.domain.repository.TenantRepository;
import com.reviq.tenancy.domain.repository.TenantSubscriptionRepository;
import com.reviq.tenancy.infrastructure.config.PaddleConfig;
import com.reviq.tenancy.infrastructure.payment.paddle.PaddleService;
import com.reviq.tenancy.shared.enums.BillingCycle;
import com.reviq.tenancy.shared.enums.SubscriptionStatus;
import com.reviq.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaddleCheckoutService {

    private final TenantRepository tenantRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PaddleService paddleService;
    private final PaddleConfig paddleConfig;

    public record CheckoutResult(String transactionId, String checkoutUrl, boolean subscriptionUpdated) {}

    public CheckoutResult createCheckout(UUID tenantId, String planName, BillingCycle billingCycle) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantId));

        SubscriptionPlan plan = planRepository.findByName(planName)
                .orElseThrow(() -> new NotFoundException("PLAN_NOT_FOUND", "Plan not found: " + planName));

        String priceId = billingCycle == BillingCycle.YEARLY
                ? plan.getPaddleYearlyPriceId()
                : plan.getPaddleMonthlyPriceId();

        if (priceId == null) {
            throw new IllegalStateException("Plan '" + planName + "' has no Paddle price for " + billingCycle);
        }

        var existingSub = subscriptionRepository.findByTenantId(tenantId).orElse(null);

        // If active subscription with Paddle ID — update via API
        if (existingSub != null && existingSub.getPaddleSubscriptionId() != null
                && existingSub.isActive()) {
            paddleService.updateSubscription(existingSub.getPaddleSubscriptionId(), priceId);
            existingSub.setPlan(plan);
            existingSub.setBillingCycle(billingCycle);
            subscriptionRepository.save(existingSub);
            log.info("Updated existing Paddle subscription for tenant: {}", tenant.getCode());
            return new CheckoutResult(null, null, true);
        }

        // Create or get Paddle customer
        String customerId = existingSub != null ? existingSub.getPaddleCustomerId() : null;
        if (customerId == null) {
            var customer = paddleService.createCustomer(
                    tenant.getCode() + "@reviq.com",
                    tenant.getOrganizationName(),
                    Map.of("tenant_id", tenant.getId().toString())
            );
            customerId = customer != null ? customer.id() : null;
        }

        // Create transaction
        var transaction = paddleService.createTransaction(
                customerId,
                priceId,
                Map.of("tenant_id", tenant.getId().toString(), "billing_cycle", billingCycle.name())
        );

        // Create or update local subscription
        if (existingSub == null) {
            existingSub = TenantSubscription.builder()
                    .tenant(tenant)
                    .plan(plan)
                    .status(SubscriptionStatus.PENDING)
                    .billingCycle(billingCycle)
                    .paddleCustomerId(customerId)
                    .build();
        } else {
            existingSub.setPlan(plan);
            existingSub.setStatus(SubscriptionStatus.PENDING);
            existingSub.setBillingCycle(billingCycle);
            existingSub.setPaddleCustomerId(customerId);
        }
        subscriptionRepository.save(existingSub);

        String checkoutUrl = paddleConfig.getCheckoutPageUrl()
                + "?transaction_id=" + transaction.id()
                + "&price_id=" + priceId;

        log.info("Created checkout for tenant '{}': {}", tenant.getCode(), checkoutUrl);
        return new CheckoutResult(transaction.id(), checkoutUrl, false);
    }

    public void cancelSubscription(UUID tenantId, boolean immediately) {
        var subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("SUBSCRIPTION_NOT_FOUND", "No subscription for tenant: " + tenantId));

        if (subscription.getPaddleSubscriptionId() != null) {
            paddleService.cancelSubscription(subscription.getPaddleSubscriptionId(), immediately);
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setCanceledAt(java.time.LocalDateTime.now());
        subscription.setAutoRenew(false);

        if (!immediately && subscription.getEndDate() != null) {
            subscription.setAccessEndsAt(subscription.getEndDate().atStartOfDay());
        }

        subscriptionRepository.save(subscription);
        log.info("Canceled subscription for tenant: {}", tenantId);
    }
}
