package com.reviq.management.api.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyCardRequest {
    private String externalId;
    private String barcode;
    private BigDecimal balance;
    private String status;
    private String contactExternalId;
    private String currentTierExternalId;
    private List<CardTierHistoryRequest> tierHistory;
}
