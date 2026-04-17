package com.reviq.management.infrastructure.filter;

import com.reviq.shared.context.RequestContext;
import jakarta.servlet.FilterChain;

import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantCode = request.getHeader("X-Auth-Tenant-Code");
        String tenantId = request.getHeader("X-Auth-Tenant-Id");

        if (tenantCode != null && tenantId != null) {
            RequestContext.set(RequestContext.RequestContextData.builder()
                    .tenantCode(tenantCode)
                    .tenantId(UUID.fromString(tenantId))
                    .build());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (tenantCode != null) {
                RequestContext.clear();
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/management/ingest");
    }
}
