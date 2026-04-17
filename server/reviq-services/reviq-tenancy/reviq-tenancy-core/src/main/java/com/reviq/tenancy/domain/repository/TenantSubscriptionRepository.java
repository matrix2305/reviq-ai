package com.reviq.tenancy.domain.repository;

import com.reviq.tenancy.domain.entity.TenantSubscription;
import com.reviq.tenancy.shared.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, UUID> {

    Optional<TenantSubscription> findByTenantId(UUID tenantId);

    Optional<TenantSubscription> findByPaddleSubscriptionId(String paddleSubscriptionId);

    Optional<TenantSubscription> findByPaddleCustomerId(String paddleCustomerId);

    List<TenantSubscription> findByStatusAndTrialEndDateBefore(SubscriptionStatus status, LocalDate date);

    List<TenantSubscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);
}
