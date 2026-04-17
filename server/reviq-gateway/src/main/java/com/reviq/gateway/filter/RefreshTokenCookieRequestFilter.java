package com.reviq.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * For web clients calling POST /api/auth/refresh with refresh_token cookie but empty body:
 * - Reads refresh_token from cookie
 * - Injects {"refreshToken":"..."} as request body
 */
@Slf4j
@Component
public class RefreshTokenCookieRequestFilter implements GlobalFilter, Ordered {

    private static final String REFRESH_PATH = "/api/v1/auth/refresh";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";
    private static final String CLIENT_TYPE_WEB = "web";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (!REFRESH_PATH.equals(path) || !isWebClient(exchange)) {
            return chain.filter(exchange);
        }

        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(REFRESH_TOKEN_COOKIE);
        if (cookie == null || cookie.getValue().isBlank()) {
            return chain.filter(exchange);
        }

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("refreshToken", cookie.getValue());
            byte[] bytes = objectMapper.writeValueAsBytes(body);

            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

            ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return Flux.just(buffer);
                }

                @Override
                public org.springframework.http.HttpHeaders getHeaders() {
                    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.putAll(super.getHeaders());
                    headers.setContentLength(bytes.length);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    return headers;
                }
            };

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.warn("Failed to inject refresh token from cookie: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }

    private boolean isWebClient(ServerWebExchange exchange) {
        String clientType = exchange.getRequest().getHeaders().getFirst(CLIENT_TYPE_HEADER);
        return CLIENT_TYPE_WEB.equalsIgnoreCase(clientType);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
