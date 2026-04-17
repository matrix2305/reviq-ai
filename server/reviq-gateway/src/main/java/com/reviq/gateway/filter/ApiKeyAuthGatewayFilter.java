package com.reviq.gateway.filter;

import com.reviq.tenancy.grpc.TenancyServiceGrpc;
import com.reviq.tenancy.grpc.ValidateApiKeyRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ApiKeyAuthGatewayFilter implements GlobalFilter, Ordered {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final List<String> API_KEY_PATHS = List.of(
            "/api/v1/management/ingest/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${reviq.gateway.grpc.tenancy-host:localhost}")
    private String tenancyGrpcHost;

    @Value("${reviq.gateway.grpc.tenancy-port:9082}")
    private int tenancyGrpcPort;

    private ManagedChannel channel;
    private TenancyServiceGrpc.TenancyServiceBlockingStub tenancyStub;

    @PostConstruct
    void init() {
        channel = ManagedChannelBuilder.forAddress(tenancyGrpcHost, tenancyGrpcPort)
                .usePlaintext()
                .build();
        tenancyStub = TenancyServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    void shutdown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (!isApiKeyPath(path)) {
            return chain.filter(exchange);
        }

        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Missing X-Api-Key header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return Mono.fromCallable(() -> tenancyStub.validateApiKey(
                        ValidateApiKeyRequest.newBuilder()
                                .setRawKey(apiKey)
                                .build()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response -> {
                    if (!response.getValid()) {
                        log.debug("Invalid API key for path: {}", path);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-Auth-Tenant-Code", response.getTenantCode())
                            .header("X-Auth-Tenant-Id", response.getTenantId())
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> {
                    log.error("API key validation failed: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isApiKeyPath(String path) {
        return API_KEY_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
