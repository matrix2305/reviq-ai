package com.reviq.management.domain.partner.entity;

import com.reviq.shared.entity.SyncableEntity;
import com.reviq.shared.enums.PartnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "partner")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Partner extends SyncableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PartnerType type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "phone")
    private String phone;

    @Column(name = "fax")
    private String fax;
}
