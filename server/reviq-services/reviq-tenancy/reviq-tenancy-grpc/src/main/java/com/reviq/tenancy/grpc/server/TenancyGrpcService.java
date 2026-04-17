package com.reviq.tenancy.grpc.server;

import com.reviq.tenancy.api.TenantApiKeyService;
import com.reviq.tenancy.domain.entity.Tenant;
import com.reviq.tenancy.domain.entity.TenantDatabase;
import com.reviq.tenancy.shared.enums.TenantStatus;
import com.reviq.tenancy.domain.repository.TenantRepository;
import com.reviq.tenancy.grpc.ConnectionInfoResponse;
import com.reviq.tenancy.grpc.GetConnectionRequest;
import com.reviq.tenancy.grpc.GetTenantByIdRequest;
import com.reviq.tenancy.grpc.ListActiveTenantsRequest;
import com.reviq.tenancy.grpc.ListActiveTenantsResponse;
import com.reviq.tenancy.grpc.TenancyServiceGrpc;
import com.reviq.tenancy.grpc.TenantInfo;
import com.reviq.tenancy.grpc.ValidateApiKeyRequest;
import com.reviq.tenancy.grpc.ValidateApiKeyResponse;
import java.util.UUID;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class TenancyGrpcService extends TenancyServiceGrpc.TenancyServiceImplBase {

    private final TenantRepository tenantRepository;
    private final TenantApiKeyService apiKeyService;

    @Override
    public void getConnectionInfo(GetConnectionRequest request,
                                   StreamObserver<ConnectionInfoResponse> responseObserver) {
        String tenantCode = request.getTenantCode();
        log.debug("gRPC GetConnectionInfo for tenant: {}", tenantCode);

        Tenant tenant = tenantRepository.findByCode(tenantCode).orElse(null);

        if (tenant == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Tenant not found: " + tenantCode)
                    .asRuntimeException());
            return;
        }

        TenantDatabase db = tenant.getDatabase();
        if (db == null) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Tenant database not configured: " + tenantCode)
                    .asRuntimeException());
            return;
        }

        ConnectionInfoResponse response = ConnectionInfoResponse.newBuilder()
                .setTenantCode(tenant.getCode())
                .setTenantId(tenant.getId().toString())
                .setDbHost(db.getDbHost())
                .setDbPort(db.getDbPort())
                .setDbName(db.getDbName())
                .setDbUsername(db.getDbUsername())
                .setDbPassword(db.getDbPassword())
                .setStatus(tenant.getStatus().name())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTenantById(GetTenantByIdRequest request,
                               StreamObserver<TenantInfo> responseObserver) {
        log.debug("gRPC GetTenantById: {}", request.getTenantId());

        Tenant tenant = tenantRepository.findById(UUID.fromString(request.getTenantId())).orElse(null);

        if (tenant == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Tenant not found: " + request.getTenantId())
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(TenantInfo.newBuilder()
                .setId(tenant.getId().toString())
                .setCode(tenant.getCode())
                .setDisplayName(tenant.getOrganizationName())
                .setStatus(tenant.getStatus().name())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void listActiveTenants(ListActiveTenantsRequest request,
                                   StreamObserver<ListActiveTenantsResponse> responseObserver) {
        log.debug("gRPC ListActiveTenants");

        var tenants = tenantRepository.findAllByStatus(TenantStatus.ACTIVE);

        ListActiveTenantsResponse.Builder responseBuilder = ListActiveTenantsResponse.newBuilder();
        for (Tenant tenant : tenants) {
            responseBuilder.addTenants(TenantInfo.newBuilder()
                    .setId(tenant.getId().toString())
                    .setCode(tenant.getCode())
                    .setDisplayName(tenant.getOrganizationName())
                    .setStatus(tenant.getStatus().name())
                    .build());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void validateApiKey(ValidateApiKeyRequest request,
                                StreamObserver<ValidateApiKeyResponse> responseObserver) {
        log.debug("gRPC ValidateApiKey");

        var result = apiKeyService.validateApiKey(request.getRawKey());

        ValidateApiKeyResponse.Builder builder = ValidateApiKeyResponse.newBuilder()
                .setValid(result.valid());

        if (result.valid()) {
            builder.setTenantCode(result.tenantCode())
                    .setTenantId(result.tenantId().toString())
                    .setKeyName(result.keyName());
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
