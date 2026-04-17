package com.reviq.shared.context;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

public class RequestContext {

    private static final InheritableThreadLocal<RequestContextData> CURRENT = new InheritableThreadLocal<>();

    public static void set(RequestContextData data) {
        CURRENT.set(data);
    }

    public static RequestContextData get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static boolean isSet() {
        return CURRENT.get() != null;
    }

    // Tenant shortcuts
    public static String getTenantCode() {
        RequestContextData data = CURRENT.get();
        return data != null ? data.getTenantCode() : null;
    }

    public static UUID getTenantId() {
        RequestContextData data = CURRENT.get();
        return data != null ? data.getTenantId() : null;
    }

    public static boolean hasTenant() {
        return getTenantCode() != null;
    }

    // User shortcuts
    public static UUID getUserId() {
        RequestContextData data = CURRENT.get();
        return data != null ? data.getUserId() : null;
    }

    public static String getEmail() {
        RequestContextData data = CURRENT.get();
        return data != null ? data.getEmail() : null;
    }

    public static String getRole() {
        RequestContextData data = CURRENT.get();
        return data != null ? data.getRole() : null;
    }

    public static boolean isPlatformAdmin() {
        return "ADMIN".equals(getRole()) && !hasTenant();
    }

    @Getter
    @Builder
    public static class RequestContextData {
        private final UUID userId;
        private final String email;
        private final String role;
        private final Set<String> permissions;
        private final String tenantCode;
        private final UUID tenantId;
    }
}
