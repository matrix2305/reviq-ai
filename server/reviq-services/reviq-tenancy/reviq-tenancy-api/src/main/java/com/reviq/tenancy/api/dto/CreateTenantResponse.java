package com.reviq.tenancy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantResponse {

    private TenantDto tenant;
    private String checkoutUrl;
}
