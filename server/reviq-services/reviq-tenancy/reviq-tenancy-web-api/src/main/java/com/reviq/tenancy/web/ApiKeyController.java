package com.reviq.tenancy.web;

import com.reviq.security.annotation.CheckRole;
import com.reviq.tenancy.api.TenantApiKeyService;
import com.reviq.tenancy.api.dto.ApiKeyCreatedResponse;
import com.reviq.tenancy.api.dto.ApiKeyDto;
import com.reviq.tenancy.api.dto.CreateApiKeyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/api-keys")
@RequiredArgsConstructor
@CheckRole("ADMIN")
public class ApiKeyController {

    private final TenantApiKeyService apiKeyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyCreatedResponse createApiKey(@RequestBody CreateApiKeyRequest request) {
        return apiKeyService.createApiKey(request);
    }

    @GetMapping("/by-tenant/{tenantCode}")
    public List<ApiKeyDto> listByTenant(@PathVariable String tenantCode) {
        return apiKeyService.findByTenantCode(tenantCode);
    }

    @DeleteMapping("/{id}")
    public ApiKeyDto revokeApiKey(@PathVariable UUID id) {
        return apiKeyService.revokeApiKey(id);
    }
}
