package com.reviq.management.domain.loyalty.repository;

import com.reviq.management.domain.loyalty.entity.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, UUID> {

    Optional<LoyaltyTier> findByExternalId(String externalId);
}
