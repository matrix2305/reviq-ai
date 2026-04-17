package com.reviq.shared.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "data-seeder.run", havingValue = "true")
public class SeederCoordinator implements CommandLineRunner {

    private static final String LOCK_KEY_PREFIX = "reviq:seeder:";
    private static final Duration LOCK_TTL = Duration.ofMinutes(30);

    private final List<Seeder> seeders;
    private final StringRedisTemplate redisTemplate;
    private final SeederProperties seederProperties;

    @Override
    public void run(String... args) {
        String lockKey = LOCK_KEY_PREFIX + seederProperties.getLockName();
        String lockValue = UUID.randomUUID().toString();

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, LOCK_TTL);

        if (!Boolean.TRUE.equals(acquired)) {
            log.info("Seeder lock '{}' already held by another instance, skipping", lockKey);
            return;
        }

        log.info("Acquired seeder lock '{}', running {} seeders", lockKey, seeders.size());

        try {
            seeders.stream()
                    .sorted(Comparator.comparingInt(Seeder::getOrder))
                    .forEach(seeder -> {
                        try {
                            log.info("Running seeder: {}", seeder.getName());
                            seeder.seed();
                            log.info("Seeder completed: {}", seeder.getName());
                        } catch (Exception e) {
                            log.error("Seeder failed: {} - {}", seeder.getName(), e.getMessage(), e);
                        }
                    });

            log.info("All seeders completed");
        } finally {
            String currentValue = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(lockKey);
                log.debug("Released seeder lock '{}'", lockKey);
            }
        }
    }
}
