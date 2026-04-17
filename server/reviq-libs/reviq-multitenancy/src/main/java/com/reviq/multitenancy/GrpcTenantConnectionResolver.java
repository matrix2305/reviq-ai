package com.reviq.multitenancy;

import com.reviq.multitenancy.config.TenancyProperties;
import com.reviq.tenancy.grpc.GetConnectionRequest;
import com.reviq.tenancy.grpc.TenancyServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GrpcTenantConnectionResolver implements TenantConnectionResolver {

    private final TenancyProperties properties;
    private final TenancyServiceGrpc.TenancyServiceBlockingStub tenancyStub;

    @Override
    public ConnectionInfo resolve(String tenantId) {
        log.debug("Resolving connection for tenant via gRPC: {}", tenantId);

        var response = tenancyStub.getConnectionInfo(
                GetConnectionRequest.newBuilder()
                        .setTenantCode(tenantId)
                        .build()
        );

        return new ConnectionInfo(
                tenantId,
                response.getDbHost(),
                response.getDbPort(),
                response.getDbName(),
                response.getDbUsername(),
                response.getDbPassword(),
                properties.getSchemaName()
        );
    }
}
