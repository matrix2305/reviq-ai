package com.reviq.tenancy.api.event;

import java.util.UUID;

public record TenantProvisioningCompletedEvent(
        UUID tenantId,
        String code
) {
}
