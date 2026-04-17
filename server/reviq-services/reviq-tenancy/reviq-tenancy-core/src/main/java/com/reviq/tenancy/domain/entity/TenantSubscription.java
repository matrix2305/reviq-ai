package com.reviq.tenancy.domain.entity;

import com.reviq.shared.entity.BaseEntity;
import com.reviq.tenancy.shared.enums.BillingCycle;
import com.reviq.tenancy.shared.enums.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_subscription")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSubscription extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;

    @Column(name = "price_at_purchase", precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    @Column(name = "currency")
    @Builder.Default
    private String currency = "EUR";

    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private Boolean autoRenew = true;

    @Column(name = "paddle_subscription_id")
    private String paddleSubscriptionId;

    @Column(name = "paddle_customer_id")
    private String paddleCustomerId;

    @Column(name = "custom_max_tokens_per_session")
    private Integer customMaxTokensPerSession;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "access_ends_at")
    private LocalDateTime accessEndsAt;

    public boolean isActive() {
        if (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL) {
            return true;
        }
        return status == SubscriptionStatus.CANCELED
                && accessEndsAt != null
                && accessEndsAt.isAfter(LocalDateTime.now());
    }

    public int getEffectiveMaxTokensPerSession() {
        if (customMaxTokensPerSession != null) {
            return customMaxTokensPerSession;
        }
        return plan.getMaxTokensPerSession();
    }
}
