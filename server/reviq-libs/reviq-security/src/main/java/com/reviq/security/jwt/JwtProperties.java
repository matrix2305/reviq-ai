package com.reviq.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "reviq.security.jwt")
public class JwtProperties {

    private String secret = "default-secret-change-in-production-min-256-bits!!";
    private long expiration = 3600;
    private long refreshExpiration = 604800;
    private String[] publicPaths = {"/api/v1/auth/**", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};
}
