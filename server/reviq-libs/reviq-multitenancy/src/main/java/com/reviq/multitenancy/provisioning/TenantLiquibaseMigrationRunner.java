package com.reviq.multitenancy.provisioning;

import com.reviq.tenancy.grpc.ConnectionInfoResponse;
import com.reviq.tenancy.grpc.GetConnectionRequest;
import com.reviq.tenancy.grpc.ListActiveTenantsRequest;
import com.reviq.tenancy.grpc.TenancyServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@RequiredArgsConstructor
public class TenantLiquibaseMigrationRunner {

    private final SchemaProvisioner schemaProvisioner;
    private final TenancyServiceGrpc.TenancyServiceBlockingStub tenancyStub;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateAllTenants() {
        log.info("Starting Liquibase migrations for all active tenants (schema: {})",
                schemaProvisioner.getSchemaName());

        var tenantsResponse = tenancyStub.listActiveTenants(
                ListActiveTenantsRequest.newBuilder().build());

        int total = tenantsResponse.getTenantsList().size();
        int success = 0;
        int failed = 0;

        for (var tenant : tenantsResponse.getTenantsList()) {
            try {
                ConnectionInfoResponse connResponse = tenancyStub.getConnectionInfo(
                        GetConnectionRequest.newBuilder()
                                .setTenantCode(tenant.getCode())
                                .build());

                String jdbcUrl = "jdbc:postgresql://" + connResponse.getDbHost()
                        + ":" + connResponse.getDbPort()
                        + "/" + connResponse.getDbName();

                schemaProvisioner.provisionSchema(
                        jdbcUrl,
                        connResponse.getDbUsername(),
                        connResponse.getDbPassword()
                );

                success++;
                log.info("Migrated tenant [{}/{}]: {}", success + failed, total, tenant.getCode());
            } catch (Exception e) {
                failed++;
                log.error("Failed to migrate tenant '{}': {}", tenant.getCode(), e.getMessage(), e);
            }
        }

        log.info("Tenant Liquibase migration complete. Total: {}, Success: {}, Failed: {}",
                total, success, failed);
    }
}
