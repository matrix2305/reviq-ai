package com.reviq.management.sync.service;

import com.reviq.management.domain.location.entity.Location;
import com.reviq.management.domain.location.repository.LocationRepository;
import com.reviq.management.sync.adapter.ErpStoreAdapter;
import com.reviq.shared.enums.LocationType;
import com.reviq.shared.enums.SyncStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationSyncService {

    private final ErpStoreAdapter erpStoreAdapter;
    private final LocationRepository locationRepository;

    @Transactional
    public SyncResult syncLocations() {
        log.info("Starting location sync...");
        var rows = erpStoreAdapter.fetchAllStores();
        long processed = 0;
        long failed = 0;

        for (Map<String, Object> row : rows) {
            try {
                String code = (String) row.get("Sifra");
                Location location = locationRepository.findByCode(code)
                        .orElseGet(Location::new);

                location.setCode(code);
                location.setName((String) row.get("Naziv"));
                location.setStreet((String) row.get("Ulica"));
                location.setCity((String) row.get("Mjesto"));
                location.setType(LocationType.STORE);
                location.setExternalId(code);
                location.setSyncedAt(LocalDateTime.now());

                locationRepository.save(location);
                processed++;
            } catch (Exception e) {
                log.error("Failed to sync location: {}", row, e);
                failed++;
            }
        }

        // Second pass: set parent relationships
        for (Map<String, Object> row : rows) {
            try {
                String code = (String) row.get("Sifra");
                String parentCode = (String) row.get("Otac");
                if (parentCode != null && !parentCode.equals(code)) {
                    locationRepository.findByCode(code).ifPresent(location -> {
                        locationRepository.findByCode(parentCode).ifPresent(location::setParent);
                        locationRepository.save(location);
                    });
                }
            } catch (Exception e) {
                log.error("Failed to set location parent: {}", row, e);
            }
        }

        log.info("Location sync complete. Processed: {}, Failed: {}", processed, failed);
        return new SyncResult(processed, failed);
    }

    public record SyncResult(long processed, long failed) {
        public SyncStatus status() {
            return failed == 0 ? SyncStatus.COMPLETED : SyncStatus.FAILED;
        }
    }
}
