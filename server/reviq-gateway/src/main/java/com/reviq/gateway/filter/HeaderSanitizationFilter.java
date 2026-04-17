package com.reviq.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Strips all X-Auth-* headers from incoming requests to prevent clients from spoofing
 * internal gateway headers. Must run BEFORE JwtAuthGatewayFilter.
 */
@Component
public class HeaderSanitizationFilter implements GlobalFilter, Ordered {

    private static final String AUTH_HEADER_PREFIX = "X-Auth-";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest sanitized = exchange.getRequest().mutate()
                .headers(headers -> headers.keySet().removeIf(
                        name -> name.startsWith(AUTH_HEADER_PREFIX)))
                .build();

        return chain.filter(exchange.mutate().request(sanitized).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
