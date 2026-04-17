package com.reviq.security.refresh;

import com.reviq.security.jwt.JwtProperties;
import com.reviq.security.model.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:";
    private static final String DELIMITER = "|";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public String createRefreshToken(AuthenticatedUser user) {
        String token = UUID.randomUUID().toString();
        String key = KEY_PREFIX + token;
        String value = serializeUser(user);
        redisTemplate.opsForValue().set(key, value,
                Duration.ofSeconds(jwtProperties.getRefreshExpiration()));
        log.debug("Created refresh token for user '{}'", user.getEmail());
        return token;
    }

    public AuthenticatedUser validateAndConsume(String refreshToken) {
        String key = KEY_PREFIX + refreshToken;
        String value = redisTemplate.opsForValue().getAndDelete(key);
        if (value == null) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
        log.debug("Consumed refresh token, issuing new pair");
        return deserializeUser(value);
    }

    private String serializeUser(AuthenticatedUser user) {
        return String.join(DELIMITER,
                user.getUserId().toString(),
                user.getTenantCode() != null ? user.getTenantCode() : "",
                user.getTenantId() != null ? user.getTenantId().toString() : "",
                user.getEmail(),
                user.getRole() != null ? user.getRole() : "",
                user.getPermissions() != null ? String.join(",", user.getPermissions()) : "");
    }

    private AuthenticatedUser deserializeUser(String value) {
        String[] parts = value.split("\\|", -1);
        return AuthenticatedUser.builder()
                .userId(UUID.fromString(parts[0]))
                .tenantCode(parts[1].isEmpty() ? null : parts[1])
                .tenantId(parts[2].isEmpty() ? null : UUID.fromString(parts[2]))
                .email(parts[3])
                .role(parts[4].isEmpty() ? null : parts[4])
                .permissions(parts[5].isEmpty() ? new LinkedHashSet<>() :
                        Arrays.stream(parts[5].split(","))
                                .collect(Collectors.toCollection(LinkedHashSet::new)))
                .build();
    }
}
