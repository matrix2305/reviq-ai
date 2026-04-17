package com.reviq.multitenancy.grpc;

import com.reviq.shared.context.RequestContext;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import java.util.UUID;

public class TenantGrpcServerInterceptor implements ServerInterceptor {

    @Override
    public <Req, Resp> ServerCall.Listener<Req> interceptCall(
            ServerCall<Req, Resp> call,
            Metadata headers,
            ServerCallHandler<Req, Resp> next) {

        String tenantCode = headers.get(GrpcTenantConstants.TENANT_CODE_METADATA_KEY);
        String tenantIdStr = headers.get(GrpcTenantConstants.TENANT_ID_METADATA_KEY);

        if ((tenantCode == null || tenantCode.isBlank()) && (tenantIdStr == null || tenantIdStr.isBlank())) {
            call.close(Status.UNAUTHENTICATED
                    .withDescription("Missing required gRPC metadata: x-tenant-code or x-tenant-id"), headers);
            return new ServerCall.Listener<>() {};
        }

        UUID tenantId = tenantIdStr != null && !tenantIdStr.isBlank() ? UUID.fromString(tenantIdStr) : null;

        Context ctx = Context.current()
                .withValue(GrpcTenantConstants.TENANT_CODE_CONTEXT_KEY, tenantCode);

        return Contexts.interceptCall(ctx, call, headers, new ServerCallHandler<>() {
            @Override
            public ServerCall.Listener<Req> startCall(
                    ServerCall<Req, Resp> serverCall, Metadata metadata) {

                RequestContext.set(RequestContext.RequestContextData.builder()
                        .tenantCode(tenantCode)
                        .tenantId(tenantId)
                        .build());

                ServerCall.Listener<Req> delegate = next.startCall(serverCall, metadata);

                return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
                    @Override
                    public void onComplete() {
                        try {
                            super.onComplete();
                        } finally {
                            RequestContext.clear();
                        }
                    }

                    @Override
                    public void onCancel() {
                        try {
                            super.onCancel();
                        } finally {
                            RequestContext.clear();
                        }
                    }
                };
            }
        });
    }
}
