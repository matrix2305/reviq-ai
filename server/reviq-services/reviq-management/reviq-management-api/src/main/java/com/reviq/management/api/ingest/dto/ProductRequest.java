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
public class ProductRequest {
    private String externalId;
    private String code;
    private String name;
    private String type;
    private String manufacturer;
    private String baseCode;
    private String brandCode;
    private String categoryCode;
    private BigDecimal purchasePrice;
    private Boolean active;
}
