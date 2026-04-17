package com.reviq.management.api.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private String externalId;
    private Long transactionNumber;
    private String type;
    private String channel;
    private Integer documentType;
    private BigDecimal discount;
    private BigDecimal amount;
    private BigDecimal netAmount;
    private BigDecimal refund;
    private LocalDateTime transactionTime;
    private LocalDateTime createdTime;
    private String accountExternalId;
    private String registerSessionExternalId;
    private String loyaltyCardExternalId;
    private String partnerExternalId;
    private Integer loyaltyPointsMaster;
    private Integer loyaltyPointsPromo;
    private Boolean isPreorder;
    private String onlineOrderId;
    private List<TransactionLineRequest> lines;
    private List<TransactionPaymentRequest> payments;
}
