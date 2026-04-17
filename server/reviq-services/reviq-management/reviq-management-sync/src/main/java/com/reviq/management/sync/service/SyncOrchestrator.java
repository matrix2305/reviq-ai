package com.reviq.management.sync.service;

import com.reviq.management.sync.model.SyncJob;
import com.reviq.management.sync.model.SyncJobRepository;
import com.reviq.shared.enums.SyncStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncOrchestrator {

    private final LocationSyncService locationSyncService;
    private final ProductSyncService productSyncService;
    private final SyncJobRepository syncJobRepository;

    @Transactional
    public void runFullSync() {
        log.info("=== Starting full ERP sync ===");

        runDomainSync("LOCATION", () -> locationSyncService.syncLocations());
        runDomainSync("BRAND", () -> productSyncService.syncBrands());
        runDomainSync("CATEGORY", () -> productSyncService.syncCategories());
        runDomainSync("PRODUCT", () -> productSyncService.syncProducts());

        log.info("=== Full ERP sync complete ===");
    }

    private void runDomainSync(String domain, java.util.function.Supplier<LocationSyncService.SyncResult> syncFn) {
        SyncJob job = SyncJob.builder()
                .domain(domain)
                .status(SyncStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        syncJobRepository.save(job);

        try {
            var result = syncFn.get();
            job.setStatus(result.status());
            job.setRecordsProcessed(result.processed());
            job.setRecordsFailed(result.failed());
        } catch (Exception e) {
            log.error("Sync failed for domain: {}", domain, e);
            job.setStatus(SyncStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        } finally {
            job.setFinishedAt(LocalDateTime.now());
            syncJobRepository.save(job);
        }
    }
}
