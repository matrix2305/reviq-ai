package com.reviq.gateway.filter;

import com.reviq.gateway.jwt.ReactiveJwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/**",
            "/api/v1/management/ingest/**",
            "/webhooks/**",
            "/actuator/**"
    );

    private final ReactiveJwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        if (!jwtTokenProvider.validateToken(token)) {
            log.debug("Invalid JWT token for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Claims claims = jwtTokenProvider.parseClaims(token);

        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                .header(HttpHeaders.AUTHORIZATION, authHeader);

        addHeaderIfPresent(requestBuilder, "X-Auth-User-Id", jwtTokenProvider.getUserId(claims));
        addHeaderIfPresent(requestBuilder, "X-Auth-Tenant-Code", jwtTokenProvider.getTenantCode(claims));
        addHeaderIfPresent(requestBuilder, "X-Auth-Tenant-Id", jwtTokenProvider.getTenantId(claims));
        addHeaderIfPresent(requestBuilder, "X-Auth-Email", jwtTokenProvider.getEmail(claims));
        addHeaderIfPresent(requestBuilder, "X-Auth-Role", jwtTokenProvider.getRole(claims));

        List<String> permissions = jwtTokenProvider.getPermissions(claims);
        if (!permissions.isEmpty()) {
            requestBuilder.header("X-Auth-Permissions", String.join(",", permissions));
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    private void addHeaderIfPresent(ServerHttpRequest.Builder builder, String name, String value) {
        if (value != null) {
            builder.header(name, value);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
