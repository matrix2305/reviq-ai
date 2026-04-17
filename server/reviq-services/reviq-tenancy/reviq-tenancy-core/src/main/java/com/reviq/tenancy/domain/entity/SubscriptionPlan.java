package com.reviq.tenancy.domain.entity;

import com.reviq.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plan")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "monthly_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal monthlyPrice;

    @Column(name = "yearly_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal yearlyPrice;

    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "EUR";

    @Column(name = "trial_days")
    @Builder.Default
    private Integer trialDays = 0;

    @Column(name = "max_tokens_per_session", nullable = false)
    private Integer maxTokensPerSession;

    @Column(name = "paddle_product_id")
    private String paddleProductId;

    @Column(name = "paddle_monthly_price_id")
    private String paddleMonthlyPriceId;

    @Column(name = "paddle_yearly_price_id")
    private String paddleYearlyPriceId;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
