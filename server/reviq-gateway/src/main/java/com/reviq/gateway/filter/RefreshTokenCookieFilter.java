package com.reviq.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * For web clients (X-Client-Type: web), intercepts login/refresh responses:
 * - Extracts refreshToken from JSON body
 * - Sets it as HttpOnly Secure cookie
 * - Removes refreshToken from JSON body
 *
 * For mobile clients (no header or X-Client-Type: mobile):
 * - Passes response through unchanged (refreshToken stays in body)
 *
 * On /api/auth/logout for web clients:
 * - Clears the refresh_token cookie
 */
@Slf4j
@Component
public class RefreshTokenCookieFilter implements GlobalFilter, Ordered {

    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";
    private static final String CLIENT_TYPE_WEB = "web";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String REFRESH_TOKEN_FIELD = "refreshToken";
    private static final String REFRESH_EXPIRES_IN_FIELD = "refreshExpiresIn";
    private static final List<String> AUTH_PATHS = List.of("/api/v1/auth/login", "/api/v1/auth/refresh");
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (!isWebClient(exchange)) {
            return chain.filter(exchange);
        }

        if (LOGOUT_PATH.equals(path)) {
            exchange.getResponse().addCookie(buildClearCookie());
            return chain.filter(exchange);
        }

        if (AUTH_PATHS.stream().noneMatch(p -> pathMatcher.match(p, path))) {
            return chain.filter(exchange);
        }

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (!(body instanceof Flux<? extends DataBuffer> fluxBody)) {
                    return super.writeWith(body);
                }

                return super.writeWith(
                        DataBufferUtils.join(fluxBody).map(dataBuffer -> {
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);

                            try {
                                JsonNode json = objectMapper.readTree(content);
                                if (json.has(REFRESH_TOKEN_FIELD) && !json.get(REFRESH_TOKEN_FIELD).isNull()) {
                                    String refreshToken = json.get(REFRESH_TOKEN_FIELD).asText();
                                    long maxAge = json.has(REFRESH_EXPIRES_IN_FIELD)
                                            ? json.get(REFRESH_EXPIRES_IN_FIELD).asLong(604800)
                                            : 604800;

                                    getDelegate().addCookie(buildRefreshCookie(refreshToken, maxAge));

                                    ((ObjectNode) json).remove(REFRESH_TOKEN_FIELD);
                                    ((ObjectNode) json).remove(REFRESH_EXPIRES_IN_FIELD);

                                    byte[] modified = objectMapper.writeValueAsBytes(json);
                                    getDelegate().getHeaders().setContentLength(modified.length);
                                    return bufferFactory.wrap(modified);
                                }
                            } catch (Exception e) {
                                log.warn("Failed to process refresh token cookie: {}", e.getMessage());
                            }

                            return bufferFactory.wrap(content);
                        }).flux()
                );
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private boolean isWebClient(ServerWebExchange exchange) {
        String clientType = exchange.getRequest().getHeaders().getFirst(CLIENT_TYPE_HEADER);
        return CLIENT_TYPE_WEB.equalsIgnoreCase(clientType);
    }

    private ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .sameSite("Strict")
                .build();
    }

    private ResponseCookie buildClearCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
