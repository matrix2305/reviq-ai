package com.reviq.management.api.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTierRequest {
    private String externalId;
    private String code;
    private String name;
    private BigDecimal discount;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private BigDecimal thresholdAmount;
    private Boolean pointsBased;
    private String loyaltyProgram;
}
