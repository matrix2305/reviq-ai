package com.reviq.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class SyncableEntity extends BaseEntity {

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
