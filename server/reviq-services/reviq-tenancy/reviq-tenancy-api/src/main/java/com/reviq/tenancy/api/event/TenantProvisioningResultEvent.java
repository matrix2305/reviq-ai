package com.reviq.tenancy.api.event;

import java.util.UUID;

public record TenantProvisioningResultEvent(
        UUID tenantId,
        String code,
        String serviceName,
        boolean success,
        String errorMessage
) {
}
