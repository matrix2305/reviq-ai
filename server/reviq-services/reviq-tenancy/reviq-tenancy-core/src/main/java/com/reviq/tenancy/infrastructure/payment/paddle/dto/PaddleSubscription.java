package com.reviq.tenancy.infrastructure.payment.paddle.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PaddleSubscription(
        String id,
        String status,
        String customerId,
        BillingPeriod currentBillingPeriod,
        ScheduledChange scheduledChange,
        List<SubscriptionItem> items,
        Map<String, Object> customData,
        String nextBilledAt,
        String startedAt
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record BillingPeriod(
            String startsAt,
            String endsAt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ScheduledChange(
            String action,
            String effectiveAt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record SubscriptionItem(
            PaddlePrice price,
            int quantity
    ) {}
}
