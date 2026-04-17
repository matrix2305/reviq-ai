package com.reviq.identity.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviq.identity.domain.entity.Permission;
import com.reviq.identity.domain.repository.PermissionRepository;
import com.reviq.shared.seeder.Seeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionSeeder implements Seeder {

    private static final String DATA_FILE = "data/seeders/permissions.json";

    private final PermissionRepository permissionRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void seed() {
        List<PermissionSeed> seeds = loadSeeds();
        if (seeds.isEmpty()) {
            log.info("PermissionSeeder: no permissions defined yet, skipping");
            return;
        }

        int created = 0;
        int skipped = 0;

        for (PermissionSeed seed : seeds) {
            if (permissionRepository.findByCode(seed.code()).isPresent()) {
                log.debug("Permission already exists: {}", seed.code());
                skipped++;
                continue;
            }

            Permission permission = new Permission();
            permission.setCode(seed.code());
            permission.setDescription(seed.description());

            permissionRepository.save(permission);
            log.info("Created permission: {}", seed.code());
            created++;
        }

        log.info("PermissionSeeder: created={}, skipped={}", created, skipped);
    }

    private List<PermissionSeed> loadSeeds() {
        try {
            ClassPathResource resource = new ClassPathResource(DATA_FILE);
            if (!resource.exists()) {
                log.warn("Seed file not found: {}", DATA_FILE);
                return List.of();
            }
            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readValue(is, new TypeReference<>() {});
            }
        } catch (IOException e) {
            log.error("Failed to load seed file: {}", DATA_FILE, e);
            return List.of();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getName() {
        return "PermissionSeeder";
    }

    private record PermissionSeed(String code, String description) {}
}
