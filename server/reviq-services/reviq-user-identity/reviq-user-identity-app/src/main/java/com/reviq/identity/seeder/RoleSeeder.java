package com.reviq.identity.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviq.identity.domain.entity.Permission;
import com.reviq.identity.domain.entity.Role;
import com.reviq.identity.domain.repository.PermissionRepository;
import com.reviq.identity.domain.repository.RoleRepository;
import com.reviq.shared.seeder.Seeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleSeeder implements Seeder {

    private static final String DATA_FILE = "data/seeders/roles.json";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void seed() {
        List<RoleSeed> seeds = loadSeeds();
        if (seeds.isEmpty()) {
            log.warn("No roles defined in {}", DATA_FILE);
            return;
        }

        int created = 0;
        int skipped = 0;

        for (RoleSeed seed : seeds) {
            if (roleRepository.findByCode(seed.code()).isPresent()) {
                log.debug("Role already exists: {}", seed.code());
                skipped++;
                continue;
            }

            Set<Permission> permissions = resolvePermissions(seed.code(), seed.permissions());

            Role role = Role.builder()
                    .code(seed.code())
                    .name(seed.name())
                    .description(seed.description())
                    .permissions(permissions)
                    .build();

            roleRepository.save(role);
            log.info("Created role: {} with {} permissions", seed.code(), permissions.size());
            created++;
        }

        log.info("RoleSeeder: created={}, skipped={}", created, skipped);
    }

    private Set<Permission> resolvePermissions(String roleCode, List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return new HashSet<>();
        }

        Set<Permission> permissions = new HashSet<>();
        for (String code : permissionCodes) {
            permissionRepository.findByCode(code).ifPresentOrElse(
                    permissions::add,
                    () -> log.warn("Permission '{}' not found for role '{}'", code, roleCode)
            );
        }
        return permissions;
    }

    private List<RoleSeed> loadSeeds() {
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
        return 1;
    }

    @Override
    public String getName() {
        return "RoleSeeder";
    }

    private record RoleSeed(String code, String name, String description, List<String> permissions) {}
}
