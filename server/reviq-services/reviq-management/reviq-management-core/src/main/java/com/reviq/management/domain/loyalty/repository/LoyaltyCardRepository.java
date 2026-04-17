package com.reviq.management.domain.loyalty.repository;

import com.reviq.management.domain.loyalty.entity.LoyaltyCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyCardRepository extends JpaRepository<LoyaltyCard, UUID> {

    Optional<LoyaltyCard> findByExternalId(String externalId);

    Optional<LoyaltyCard> findByBarcode(String barcode);
}
