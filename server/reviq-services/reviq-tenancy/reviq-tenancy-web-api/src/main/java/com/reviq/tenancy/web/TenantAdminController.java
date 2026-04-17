package com.reviq.tenancy.web;

import com.reviq.security.annotation.CheckRole;
import com.reviq.shared.search.SearchRequest;
import com.reviq.tenancy.api.TenantManagementService;
import com.reviq.tenancy.api.dto.CreateTenantRequest;
import com.reviq.tenancy.api.dto.CreateTenantResponse;
import com.reviq.tenancy.api.dto.TenantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tenants")
@RequiredArgsConstructor
@CheckRole("ADMIN")
public class TenantAdminController {

    private final TenantManagementService tenantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTenantResponse createTenant(@RequestBody CreateTenantRequest request) {
        return tenantService.createTenant(request);
    }

    @PostMapping("/{id}/provision")
    public TenantDto provisionTenant(@PathVariable UUID id) {
        return tenantService.provisionTenant(id);
    }

    @PostMapping("/search")
    public Page<TenantDto> search(@RequestBody SearchRequest request) {
        return tenantService.search(request);
    }

    @GetMapping("/{id}")
    public TenantDto getTenant(@PathVariable UUID id) {
        return tenantService.findById(id);
    }

    @GetMapping("/by-code/{code}")
    public TenantDto getTenantByCode(@PathVariable String code) {
        return tenantService.findByCode(code);
    }

    @DeleteMapping("/{id}")
    public TenantDto decommissionTenant(@PathVariable UUID id) {
        return tenantService.decommissionTenant(id);
    }
}
