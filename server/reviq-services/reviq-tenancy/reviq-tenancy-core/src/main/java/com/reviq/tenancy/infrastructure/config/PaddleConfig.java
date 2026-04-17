package com.reviq.tenancy.infrastructure.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@Configuration
@ConfigurationProperties(prefix = "paddle")
public class PaddleConfig {

    private boolean enabled = false;
    private String apiKey;
    private String apiUrl = "https://sandbox-api.paddle.com";
    private String webhookSecret;
    private String clientToken;
    private String defaultCurrency = "EUR";
    private String successUrl;
    private String cancelUrl;
    private String checkoutPageUrl;

    @Bean("paddleWebClient")
    @ConditionalOnProperty(name = "paddle.enabled", havingValue = "true")
    public WebClient paddleWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
