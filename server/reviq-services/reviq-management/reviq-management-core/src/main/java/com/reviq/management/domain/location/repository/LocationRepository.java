package com.reviq.management.domain.location.repository;

import com.reviq.management.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByCode(String code);

    Optional<Location> findByExternalId(String externalId);
}
