package com.reviq.multitenancy.provisioning;

import com.reviq.multitenancy.ConnectionInfo;
import com.reviq.multitenancy.TenantConnectionResolver;
import com.reviq.tenancy.api.event.TenantEventConstants;
import com.reviq.tenancy.api.event.TenantProvisioningRequestedEvent;
import com.reviq.tenancy.api.event.TenantProvisioningResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTenantProvisioningListener {

    private final SchemaProvisioner schemaProvisioner;
    private final TenantConnectionResolver connectionResolver;
    private final RabbitTemplate rabbitTemplate;

    protected abstract String getServiceName();

    protected abstract String getRoutingKey();

    protected void handleProvisioningRequest(TenantProvisioningRequestedEvent event) {
        log.info("[{}] Received provisioning request for tenant: {}", getServiceName(), event.code());

        boolean success = false;
        String errorMessage = null;

        try {
            // Uses direct JDBC via ConnectionInfo, NOT TenantDataSourceHolder.
            // We are CREATING the schema — TenantDataSourceAdviceContributor may have resolved
            // a DS for a schema that doesn't exist yet, so we bypass it intentionally.
            ConnectionInfo conn = connectionResolver.resolve(event.code());
            schemaProvisioner.provisionSchema(conn.jdbcUrl(), conn.dbUsername(), conn.dbPassword());
            success = true;
            log.info("[{}] Schema provisioned for tenant: {}", getServiceName(), event.code());
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error("[{}] Failed to provision schema for tenant: {}", getServiceName(), event.code(), e);
        }

        rabbitTemplate.convertAndSend(
                TenantEventConstants.EXCHANGE,
                getRoutingKey(),
                new TenantProvisioningResultEvent(
                        event.tenantId(),
                        event.code(),
                        getServiceName(),
                        success,
                        errorMessage
                )
        );
    }
}
