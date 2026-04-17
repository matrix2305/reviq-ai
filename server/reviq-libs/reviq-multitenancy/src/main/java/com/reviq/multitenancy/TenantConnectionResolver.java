package com.reviq.multitenancy;

public interface TenantConnectionResolver {

    ConnectionInfo resolve(String tenantId);
}
