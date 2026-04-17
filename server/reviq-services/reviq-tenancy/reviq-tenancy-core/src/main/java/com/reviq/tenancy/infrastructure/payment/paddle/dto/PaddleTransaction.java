package com.reviq.tenancy.infrastructure.payment.paddle.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PaddleTransaction(
        String id,
        String status,
        String customerId,
        String subscriptionId,
        Checkout checkout,
        Map<String, Object> customData
) {

    public static final String DRAFT = "draft";
    public static final String READY = "ready";
    public static final String BILLED = "billed";
    public static final String PAID = "paid";
    public static final String COMPLETED = "completed";
    public static final String CANCELED = "canceled";

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Checkout(
            String url
    ) {}
}
