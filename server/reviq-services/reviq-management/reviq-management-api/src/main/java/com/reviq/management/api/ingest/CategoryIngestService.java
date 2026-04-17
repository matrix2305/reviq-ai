package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.CategoryRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryIngestService {
    UUID upsert(CategoryRequest request);
    List<UUID> upsertBatch(List<CategoryRequest> requests);
}
