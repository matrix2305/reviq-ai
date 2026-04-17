package com.reviq.management.domain.transaction.repository;

import com.reviq.management.domain.transaction.entity.RegisterSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegisterSessionRepository extends JpaRepository<RegisterSession, UUID> {

    Optional<RegisterSession> findByExternalId(String externalId);
}
