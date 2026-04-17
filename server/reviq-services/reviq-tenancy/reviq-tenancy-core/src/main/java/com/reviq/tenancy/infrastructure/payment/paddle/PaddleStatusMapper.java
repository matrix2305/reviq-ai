package com.reviq.tenancy.infrastructure.payment.paddle;

import com.reviq.tenancy.shared.enums.SubscriptionStatus;

import java.util.Map;

public final class PaddleStatusMapper {

    private PaddleStatusMapper() {
        // utility class
    }

    private static final Map<String, SubscriptionStatus> PADDLE_TO_STATUS = Map.of(
            "trialing", SubscriptionStatus.TRIAL,
            "active", SubscriptionStatus.ACTIVE,
            "past_due", SubscriptionStatus.PAST_DUE,
            "canceled", SubscriptionStatus.CANCELED,
            "paused", SubscriptionStatus.EXPIRED
    );

    private static final Map<SubscriptionStatus, String> STATUS_TO_PADDLE = Map.of(
            SubscriptionStatus.TRIAL, "trialing",
            SubscriptionStatus.ACTIVE, "active",
            SubscriptionStatus.PAST_DUE, "past_due",
            SubscriptionStatus.CANCELED, "canceled",
            SubscriptionStatus.EXPIRED, "paused"
    );

    public static SubscriptionStatus toSubscriptionStatus(String paddleStatus) {
        if (paddleStatus == null) {
            return SubscriptionStatus.PENDING;
        }
        return PADDLE_TO_STATUS.getOrDefault(paddleStatus.toLowerCase(), SubscriptionStatus.PENDING);
    }

    public static String toPaddleStatus(SubscriptionStatus status) {
        if (status == null) {
            return "active";
        }
        return STATUS_TO_PADDLE.getOrDefault(status, "active");
    }
}
