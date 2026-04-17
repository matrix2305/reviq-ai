package com.reviq.tenancy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDto {

    private UUID id;
    private String code;
    private String organizationName;
    private String status;
    private String type;
    private String subscriptionPlan;
    private String subscriptionStatus;
    private LocalDateTime createdAt;
}
