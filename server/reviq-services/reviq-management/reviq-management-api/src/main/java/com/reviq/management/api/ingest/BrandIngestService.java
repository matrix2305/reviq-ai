package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.BrandRequest;

import java.util.List;
import java.util.UUID;

public interface BrandIngestService {
    UUID upsert(BrandRequest request);
    List<UUID> upsertBatch(List<BrandRequest> requests);
}
