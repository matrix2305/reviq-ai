package com.reviq.tenancy.application.service;

import com.reviq.tenancy.api.TenantApiKeyService;
import com.reviq.tenancy.api.dto.ApiKeyCreatedResponse;
import com.reviq.tenancy.api.dto.ApiKeyDto;
import com.reviq.tenancy.api.dto.CreateApiKeyRequest;
import com.reviq.tenancy.application.mapper.ApiKeyMapper;
import com.reviq.tenancy.domain.entity.Tenant;
import com.reviq.tenancy.domain.entity.TenantApiKey;
import com.reviq.tenancy.domain.repository.TenantApiKeyRepository;
import com.reviq.tenancy.domain.repository.TenantRepository;
import com.reviq.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TenantApiKeyServiceImpl implements TenantApiKeyService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int KEY_LENGTH = 32;
    private static final int PREFIX_LENGTH = 8;

    private final TenantApiKeyRepository apiKeyRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyMapper apiKeyMapper;

    @Override
    public ApiKeyCreatedResponse createApiKey(CreateApiKeyRequest request) {
        Tenant tenant = tenantRepository.findByCode(request.getTenantCode())
                .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + request.getTenantCode()));

        String rawKey = generateRawKey();
        String prefix = rawKey.substring(0, PREFIX_LENGTH);
        String hash = passwordEncoder.encode(rawKey);

        TenantApiKey apiKey = TenantApiKey.builder()
                .tenant(tenant)
                .name(request.getName())
                .keyPrefix(prefix)
                .keyHash(hash)
                .expiresAt(request.getExpiresAt())
                .build();

        apiKey = apiKeyRepository.save(apiKey);
        log.info("Created API key '{}' for tenant '{}'", request.getName(), request.getTenantCode());

        return ApiKeyCreatedResponse.builder()
                .apiKey(apiKeyMapper.toDto(apiKey))
                .rawKey("rk_" + rawKey)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKeyDto> findByTenantCode(String tenantCode) {
        Tenant tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantCode));

        return apiKeyMapper.toDtoList(apiKeyRepository.findAllByTenantId(tenant.getId()));
    }

    @Override
    public ApiKeyDto revokeApiKey(UUID apiKeyId) {
        TenantApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new NotFoundException("API_KEY_NOT_FOUND", "API key not found: " + apiKeyId));

        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);

        log.info("Revoked API key '{}' ({})", apiKey.getName(), apiKey.getKeyPrefix());
        return apiKeyMapper.toDto(apiKey);
    }

    @Override
    @Transactional
    public ApiKeyValidationResult validateApiKey(String rawKey) {
        if (rawKey == null || !rawKey.startsWith("rk_")) {
            return new ApiKeyValidationResult(false, null, null, null);
        }

        String keyWithoutPrefix = rawKey.substring(3);
        if (keyWithoutPrefix.length() < PREFIX_LENGTH) {
            return new ApiKeyValidationResult(false, null, null, null);
        }

        String prefix = keyWithoutPrefix.substring(0, PREFIX_LENGTH);
        TenantApiKey apiKey = apiKeyRepository.findByKeyPrefix(prefix).orElse(null);

        if (apiKey == null || !apiKey.isValid()) {
            return new ApiKeyValidationResult(false, null, null, null);
        }

        if (!passwordEncoder.matches(keyWithoutPrefix, apiKey.getKeyHash())) {
            return new ApiKeyValidationResult(false, null, null, null);
        }

        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);

        Tenant tenant = apiKey.getTenant();
        return new ApiKeyValidationResult(true, tenant.getCode(), tenant.getId(), apiKey.getName());
    }

    private String generateRawKey() {
        byte[] bytes = new byte[KEY_LENGTH];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
