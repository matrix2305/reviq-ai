package com.reviq.tenancy.infrastructure.payment.paddle.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PaddleWebhookEvent(
        String eventId,
        String eventType,
        String occurredAt,
        String notificationId,
        Map<String, Object> data
) {

    public static final String SUBSCRIPTION_CREATED = "subscription.created";
    public static final String SUBSCRIPTION_UPDATED = "subscription.updated";
    public static final String SUBSCRIPTION_ACTIVATED = "subscription.activated";
    public static final String SUBSCRIPTION_CANCELED = "subscription.canceled";
    public static final String SUBSCRIPTION_PAUSED = "subscription.paused";
    public static final String SUBSCRIPTION_RESUMED = "subscription.resumed";
    public static final String SUBSCRIPTION_PAST_DUE = "subscription.past_due";
    public static final String SUBSCRIPTION_TRIALING = "subscription.trialing";
    public static final String TRANSACTION_COMPLETED = "transaction.completed";
    public static final String TRANSACTION_PAID = "transaction.paid";
    public static final String TRANSACTION_PAYMENT_FAILED = "transaction.payment_failed";
}
