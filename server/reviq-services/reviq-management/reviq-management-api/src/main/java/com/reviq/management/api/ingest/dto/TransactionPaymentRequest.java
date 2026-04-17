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
public class TransactionPaymentRequest {
    private String paymentMethodCode;
    private BigDecimal amount;
    private String warehouse;
    private String salesType;
}
