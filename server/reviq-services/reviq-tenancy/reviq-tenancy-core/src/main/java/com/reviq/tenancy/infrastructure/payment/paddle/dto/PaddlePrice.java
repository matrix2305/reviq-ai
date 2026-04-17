package com.reviq.tenancy.infrastructure.payment.paddle.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PaddlePrice(
        String id,
        String productId,
        String name,
        String description,
        BillingCycle billingCycle,
        TrialPeriod trialPeriod,
        UnitPrice unitPrice
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record BillingCycle(
            String interval,
            int frequency
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record TrialPeriod(
            String interval,
            int frequency
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record UnitPrice(
            String amount,
            String currencyCode
    ) {}

    public boolean isMonthly() {
        return billingCycle != null
                && "month".equalsIgnoreCase(billingCycle.interval())
                && billingCycle.frequency() == 1;
    }

    public boolean isYearly() {
        return billingCycle != null
                && "year".equalsIgnoreCase(billingCycle.interval())
                && billingCycle.frequency() == 1;
    }
}
