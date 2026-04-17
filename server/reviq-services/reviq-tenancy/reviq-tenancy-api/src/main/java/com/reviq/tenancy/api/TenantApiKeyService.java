package com.reviq.tenancy.api;

import com.reviq.tenancy.api.dto.ApiKeyCreatedResponse;
import com.reviq.tenancy.api.dto.ApiKeyDto;
import com.reviq.tenancy.api.dto.CreateApiKeyRequest;

import java.util.List;
import java.util.UUID;

public interface TenantApiKeyService {

    ApiKeyCreatedResponse createApiKey(CreateApiKeyRequest request);

    List<ApiKeyDto> findByTenantCode(String tenantCode);

    ApiKeyDto revokeApiKey(UUID apiKeyId);

    ApiKeyValidationResult validateApiKey(String rawKey);

    record ApiKeyValidationResult(boolean valid, String tenantCode, UUID tenantId, String keyName) {
    }
}
