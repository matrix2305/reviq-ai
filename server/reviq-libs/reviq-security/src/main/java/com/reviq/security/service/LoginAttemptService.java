package com.reviq.security.service;

import com.reviq.shared.exception.AccountLockedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Slf4j
public class LoginAttemptService {

    private static final String KEY_PREFIX = "login-attempt:";

    private final StringRedisTemplate redisTemplate;
    private final int maxAttempts;
    private final Duration lockoutDuration;

    public LoginAttemptService(StringRedisTemplate redisTemplate, int maxAttempts, int lockoutMinutes) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.lockoutDuration = Duration.ofMinutes(lockoutMinutes);
    }

    public void checkBlocked(String email) {
        String key = KEY_PREFIX + email.toLowerCase();
        String value = redisTemplate.opsForValue().get(key);

        if (value != null && Integer.parseInt(value) >= maxAttempts) {
            Long ttl = redisTemplate.getExpire(key);
            long remaining = ttl != null && ttl > 0 ? ttl : lockoutDuration.getSeconds();
            log.warn("AUDIT: Account locked for '{}', remaining {}s", email, remaining);
            throw new AccountLockedException(remaining);
        }
    }

    public void loginFailed(String email) {
        String key = KEY_PREFIX + email.toLowerCase();
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, lockoutDuration);
        }

        log.warn("AUDIT: Failed login attempt {} for '{}'", attempts, email);
    }

    public void loginSucceeded(String email) {
        String key = KEY_PREFIX + email.toLowerCase();
        redisTemplate.delete(key);
    }
}
