package com.reviq.gateway.config;

import com.reviq.gateway.jwt.ReactiveJwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayRouteConfig {

    @Bean
    public ReactiveJwtTokenProvider reactiveJwtTokenProvider(
            @Value("${reviq.security.jwt.secret:default-secret-change-in-production-min-256-bits!!}") String jwtSecret) {
        return new ReactiveJwtTokenProvider(jwtSecret);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, GatewayProperties props,
                                            RedisRateLimiter authRateLimiter, KeyResolver ipKeyResolver) {
        GatewayProperties.ServiceUrls services = props.getServices();

        return builder.routes()
                // Auth endpoints (public, rate-limited)
                .route("auth", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f.requestRateLimiter(rl -> rl
                                .setRateLimiter(authRateLimiter)
                                .setKeyResolver(ipKeyResolver)
                                .setDenyEmptyKey(false)))
                        .uri(services.getUserIdentity()))

                // User endpoints (authenticated)
                .route("users", r -> r
                        .path("/api/v1/users/**")
                        .uri(services.getUserIdentity()))

                // User admin endpoints
                .route("users-admin", r -> r
                        .path("/api/v1/admin/users/**")
                        .uri(services.getUserIdentity()))

                // Tenant admin endpoints
                .route("tenants-admin", r -> r
                        .path("/api/v1/admin/tenants/**")
                        .uri(services.getTenancy()))

                // API key admin endpoints
                .route("api-keys-admin", r -> r
                        .path("/api/v1/admin/api-keys/**")
                        .uri(services.getTenancy()))

                // Webhooks (public)
                .route("webhooks", r -> r
                        .path("/webhooks/**")
                        .uri(services.getTenancy()))

                // Management ingest (API key auth, no JWT)
                .route("management-ingest", r -> r
                        .path("/api/v1/management/ingest/**")
                        .uri(services.getManagement()))

                // Management service (JWT auth)
                .route("management", r -> r
                        .path("/api/v1/management/**")
                        .uri(services.getManagement()))

                // AI service
                .route("ai", r -> r
                        .path("/api/v1/ai/**")
                        .uri(services.getAi()))

                .build();
    }
}
