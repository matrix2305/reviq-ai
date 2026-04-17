package com.reviq.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ReactiveJwtTokenProvider {

    private final Key signingKey;

    public ReactiveJwtTokenProvider(String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserId(Claims claims) {
        return claims.getSubject();
    }

    public String getTenantCode(Claims claims) {
        return claims.get("tenant_code", String.class);
    }

    public String getTenantId(Claims claims) {
        return claims.get("tenant_id", String.class);
    }

    public String getEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissions(Claims claims) {
        List<String> permissions = claims.get("permissions", List.class);
        return permissions != null ? permissions : Collections.emptyList();
    }
}
