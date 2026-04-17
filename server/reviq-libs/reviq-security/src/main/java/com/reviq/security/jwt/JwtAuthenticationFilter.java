package com.reviq.security.jwt;

import com.reviq.security.model.AuthenticatedUser;
import com.reviq.shared.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final String GATEWAY_USER_ID = "X-Auth-User-Id";
    private static final String GATEWAY_TENANT_CODE = "X-Auth-Tenant-Code";
    private static final String GATEWAY_TENANT_ID = "X-Auth-Tenant-Id";
    private static final String GATEWAY_EMAIL = "X-Auth-Email";
    private static final String GATEWAY_ROLE = "X-Auth-Role";
    private static final String GATEWAY_PERMISSIONS = "X-Auth-Permissions";

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        try {
            AuthenticatedUser user = null;

            // Mode 1: JWT token present — validate and parse
            String token = extractToken(request);
            if (token != null && tokenProvider.validateToken(token)) {
                user = tokenProvider.parseToken(token);
            }

            // Mode 2: Gateway headers present — trust forwarded claims
            if (user == null) {
                user = extractFromGatewayHeaders(request);
            }

            if (user != null) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Set unified RequestContext
                RequestContext.set(RequestContext.RequestContextData.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .permissions(user.getPermissions())
                        .tenantCode(user.getTenantCode())
                        .tenantId(user.getTenantId())
                        .build());

                log.debug("Authenticated user '{}' for tenant '{}'", user.getEmail(), user.getTenantCode());
            }

            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private AuthenticatedUser extractFromGatewayHeaders(HttpServletRequest request) {
        String userId = request.getHeader(GATEWAY_USER_ID);
        if (userId == null || userId.isBlank()) {
            return null;
        }

        String permissionsHeader = request.getHeader(GATEWAY_PERMISSIONS);
        Set<String> permissions = (permissionsHeader != null && !permissionsHeader.isBlank())
                ? Arrays.stream(permissionsHeader.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toCollection(LinkedHashSet::new))
                : new LinkedHashSet<>();

        String tenantIdHeader = request.getHeader(GATEWAY_TENANT_ID);

        return AuthenticatedUser.builder()
                .userId(UUID.fromString(userId))
                .tenantCode(request.getHeader(GATEWAY_TENANT_CODE))
                .tenantId(tenantIdHeader != null ? UUID.fromString(tenantIdHeader) : null)
                .email(request.getHeader(GATEWAY_EMAIL))
                .role(request.getHeader(GATEWAY_ROLE))
                .permissions(permissions)
                .build();
    }
}
