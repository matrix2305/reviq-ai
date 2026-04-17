package com.reviq.multitenancy.grpc;

import com.reviq.shared.context.RequestContext;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.UUID;

public class TenantGrpcClientInterceptor implements ClientInterceptor {

    @Override
    public <Req, Resp> ClientCall<Req, Resp> interceptCall(
            MethodDescriptor<Req, Resp> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<Resp> responseListener, Metadata headers) {
                String tenantCode = RequestContext.getTenantCode();
                UUID tenantId = RequestContext.getTenantId();

                if (tenantCode != null) {
                    headers.put(GrpcTenantConstants.TENANT_CODE_METADATA_KEY, tenantCode);
                }
                if (tenantId != null) {
                    headers.put(GrpcTenantConstants.TENANT_ID_METADATA_KEY, tenantId.toString());
                }
                super.start(responseListener, headers);
            }
        };
    }
}
