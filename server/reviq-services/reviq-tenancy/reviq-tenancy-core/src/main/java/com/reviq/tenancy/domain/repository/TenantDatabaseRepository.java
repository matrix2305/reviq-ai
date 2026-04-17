package com.reviq.tenancy.domain.repository;

import com.reviq.tenancy.domain.entity.TenantDatabase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantDatabaseRepository extends JpaRepository<TenantDatabase, UUID> {

    Optional<TenantDatabase> findByTenantId(UUID tenantId);
}
