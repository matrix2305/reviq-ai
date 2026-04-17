package com.reviq.multitenancy.grpc;

import io.grpc.Context;
import io.grpc.Metadata;

public final class GrpcTenantConstants {

    public static final Metadata.Key<String> TENANT_CODE_METADATA_KEY =
            Metadata.Key.of("x-tenant-code", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> TENANT_ID_METADATA_KEY =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> TENANT_CODE_CONTEXT_KEY =
            Context.key("tenant-code");

    private GrpcTenantConstants() {
    }
}
