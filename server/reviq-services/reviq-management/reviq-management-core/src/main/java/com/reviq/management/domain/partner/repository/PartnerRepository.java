package com.reviq.management.domain.partner.repository;

import com.reviq.management.domain.partner.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID> {

    Optional<Partner> findByExternalId(String externalId);
}
