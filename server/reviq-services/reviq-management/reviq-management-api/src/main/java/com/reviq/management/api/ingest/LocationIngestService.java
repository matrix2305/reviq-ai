package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.LocationRequest;

import java.util.List;
import java.util.UUID;

public interface LocationIngestService {
    UUID upsert(LocationRequest request);
    List<UUID> upsertBatch(List<LocationRequest> requests);
}
