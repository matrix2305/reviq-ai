package com.reviq.security.jwt;

import com.reviq.security.model.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;

    public String generateToken(AuthenticatedUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpiration() * 1000);

        var builder = Jwts.builder()
                .setSubject(user.getUserId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .claim("permissions", user.getPermissions())
                .setIssuedAt(now)
                .setExpiration(expiry);

        if (user.getTenantCode() != null) {
            builder.claim("tenant_code", user.getTenantCode());
        }
        if (user.getTenantId() != null) {
            builder.claim("tenant_id", user.getTenantId().toString());
        }

        return builder.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    @SuppressWarnings("unchecked")
    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String tenantIdStr = claims.get("tenant_id", String.class);

        return AuthenticatedUser.builder()
                .userId(UUID.fromString(claims.getSubject()))
                .tenantCode(claims.get("tenant_code", String.class))
                .tenantId(tenantIdStr != null ? UUID.fromString(tenantIdStr) : null)
                .email(claims.get("email", String.class))
                .role(claims.get("role", String.class))
                .permissions(new HashSet<>(claims.get("permissions", List.class)))
                .build();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
