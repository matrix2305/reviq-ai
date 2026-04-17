package com.reviq.tenancy.domain.repository;

import com.reviq.tenancy.domain.entity.TenantApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantApiKeyRepository extends JpaRepository<TenantApiKey, UUID> {

    Optional<TenantApiKey> findByKeyPrefix(String keyPrefix);

    List<TenantApiKey> findAllByTenantId(UUID tenantId);

    List<TenantApiKey> findAllByTenantIdAndActiveTrue(UUID tenantId);
}
