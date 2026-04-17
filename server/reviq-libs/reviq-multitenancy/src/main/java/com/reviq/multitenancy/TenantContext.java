package com.reviq.multitenancy;

import com.reviq.shared.context.RequestContext;

/**
 * Delegate to RequestContext for backward compatibility.
 * Prefer using RequestContext directly in new code.
 */
public class TenantContext {

    public static void set(String tenantCode) {
        RequestContext.RequestContextData existing = RequestContext.get();
        if (existing != null) {
            RequestContext.set(RequestContext.RequestContextData.builder()
                    .userId(existing.getUserId())
                    .email(existing.getEmail())
                    .role(existing.getRole())
                    .permissions(existing.getPermissions())
                    .tenantCode(tenantCode)
                    .tenantId(existing.getTenantId())
                    .build());
        } else {
            RequestContext.set(RequestContext.RequestContextData.builder()
                    .tenantCode(tenantCode)
                    .build());
        }
    }

    public static String get() {
        return RequestContext.getTenantCode();
    }

    public static void clear() {
        RequestContext.clear();
    }

    public static boolean isSet() {
        return RequestContext.hasTenant();
    }
}
