package com.reviq.tenancy.api;

import com.reviq.tenancy.api.dto.CreateTenantRequest;
import com.reviq.tenancy.api.dto.CreateTenantResponse;
import com.reviq.tenancy.api.dto.TenantDto;
import com.reviq.shared.search.SearchRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface TenantManagementService {

    CreateTenantResponse createTenant(CreateTenantRequest request);

    TenantDto provisionTenant(UUID tenantId);

    Page<TenantDto> search(SearchRequest request);

    TenantDto findById(UUID id);

    TenantDto findByCode(String code);

    TenantDto decommissionTenant(UUID id);
}
