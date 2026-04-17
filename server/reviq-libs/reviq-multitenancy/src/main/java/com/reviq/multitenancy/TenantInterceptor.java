package com.reviq.multitenancy;

import com.reviq.multitenancy.datasource.TenantDataSourceCache;
import com.reviq.multitenancy.datasource.TenantDataSourceHolder;
import com.reviq.shared.context.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    private final TenantConnectionResolver resolver;
    private final TenantDataSourceCache dataSourceCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // RequestContext set by JwtAuthenticationFilter is the authoritative source
        String tenantCode = RequestContext.getTenantCode();

        // Fallback to header (for API key auth, webhooks, or non-JWT flows)
        if (tenantCode == null || tenantCode.isBlank()) {
            tenantCode = request.getHeader(TENANT_HEADER);
        }

        if (tenantCode == null || tenantCode.isBlank()) {
            log.warn("No tenant context available (JWT or {} header)", TENANT_HEADER);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing tenant context");
            return false;
        }

        log.debug("Resolving tenant: {}", tenantCode);

        ConnectionInfo info = resolver.resolve(tenantCode);
        var ds = dataSourceCache.getOrCreate(tenantCode, info);
        TenantDataSourceHolder.set(ds);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantDataSourceHolder.clear();
    }
}
