package com.reviq.identity.domain.repository;

import com.reviq.identity.domain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID>, JpaSpecificationExecutor<AppUser> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByTenantIdAndEmail(UUID tenantId, String email);

    List<AppUser> findAllByTenantId(UUID tenantId);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
}
