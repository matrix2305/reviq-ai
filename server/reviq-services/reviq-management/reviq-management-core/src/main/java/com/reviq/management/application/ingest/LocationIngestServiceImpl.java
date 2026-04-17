package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.LocationIngestService;
import com.reviq.management.api.ingest.dto.LocationRequest;
import com.reviq.management.domain.location.entity.Location;
import com.reviq.management.domain.location.repository.LocationRepository;
import com.reviq.shared.enums.LocationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationIngestServiceImpl implements LocationIngestService {

    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public UUID upsert(LocationRequest request) {
        Location location = locationRepository.findByExternalId(request.getExternalId())
                .orElseGet(Location::new);

        location.setExternalId(request.getExternalId());
        location.setCode(request.getCode());
        location.setName(request.getName());
        location.setStreet(request.getStreet());
        location.setCity(request.getCity());
        location.setZone(request.getZone());
        location.setSyncedAt(LocalDateTime.now());

        if (request.getType() != null) {
            location.setType(LocationType.valueOf(request.getType()));
        }
        if (request.getActive() != null) {
            location.setActive(request.getActive());
        }
        if (request.getParentExternalId() != null) {
            locationRepository.findByExternalId(request.getParentExternalId())
                    .ifPresent(location::setParent);
        }

        return locationRepository.save(location).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<LocationRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
