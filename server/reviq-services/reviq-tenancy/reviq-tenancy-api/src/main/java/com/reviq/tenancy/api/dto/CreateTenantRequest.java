package com.reviq.tenancy.api.dto;

import com.reviq.tenancy.shared.enums.TenantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {

    private String organizationName;
    private TenantType type;

    // Admin user
    private String adminEmail;
    private String adminPassword;
    private String adminFirstName;
    private String adminLastName;

    // Subscription
    private UUID planId;
    private String billingCycle;
}
