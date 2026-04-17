package com.reviq.management.domain.loyalty.entity;

import com.reviq.shared.entity.SyncableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_tier")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTier extends SyncableEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "discount", precision = 7, scale = 5, nullable = false)
    private BigDecimal discount;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(name = "threshold_amount", precision = 16, scale = 2)
    private BigDecimal thresholdAmount;

    @Column(name = "points_based", nullable = false)
    @Builder.Default
    private Boolean pointsBased = false;

    @Column(name = "loyalty_program", nullable = false)
    private String loyaltyProgram;
}
