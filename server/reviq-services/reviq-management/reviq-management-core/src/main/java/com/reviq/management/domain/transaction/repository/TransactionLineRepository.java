package com.reviq.management.domain.transaction.repository;

import com.reviq.management.domain.transaction.entity.TransactionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionLineRepository extends JpaRepository<TransactionLine, UUID> {
}
