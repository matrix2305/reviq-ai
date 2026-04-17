package com.reviq.tenancy.api.event;

import java.util.UUID;

public record TenantProvisioningRequestedEvent(
        UUID tenantId,
        String code
) {
}
