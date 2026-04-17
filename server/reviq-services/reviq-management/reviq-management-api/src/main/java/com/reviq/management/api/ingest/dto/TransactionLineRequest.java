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
public class TransactionLineRequest {
    private Integer lineNumber;
    private String productExternalId;
    private String lineType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal listPrice;
    private BigDecimal discount;
    private BigDecimal purchasePrice;
    private BigDecimal retailAmount;
    private BigDecimal taxAmount;
    private String warehouse;
    private String salesType;
    private Boolean earnLoyaltyPoints;
    private BigDecimal refund;
}
