package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.ProductRequest;

import java.util.List;
import java.util.UUID;

public interface ProductIngestService {
    UUID upsert(ProductRequest request);
    List<UUID> upsertBatch(List<ProductRequest> requests);
}
