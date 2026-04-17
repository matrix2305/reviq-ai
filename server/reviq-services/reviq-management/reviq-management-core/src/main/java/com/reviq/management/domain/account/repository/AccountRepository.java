package com.reviq.management.domain.account.repository;

import com.reviq.management.domain.account.entity.Account;
import com.reviq.management.domain.partner.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByExternalId(String externalId);

    List<Account> findByPartner(Partner partner);
}
