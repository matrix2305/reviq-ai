package com.reviq.management.api.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {
    private String externalId;
    private String accountNumber;
    private String type;
    private String currency;
    private String partnerExternalId;
    private String locationExternalId;
    private String productExternalId;
    private BigDecimal balance;
    private Boolean active;
}
