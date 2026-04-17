package com.reviq.management.sync.model;

import com.reviq.shared.entity.BaseEntity;
import com.reviq.shared.enums.SyncStatus;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "sync_job")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncJob extends BaseEntity {

    @Column(name = "domain", nullable = false)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SyncStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "records_processed")
    @Builder.Default
    private Long recordsProcessed = 0L;

    @Column(name = "records_failed")
    @Builder.Default
    private Long recordsFailed = 0L;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;
}
