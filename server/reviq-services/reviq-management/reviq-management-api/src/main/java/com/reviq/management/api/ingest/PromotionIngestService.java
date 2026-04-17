package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.PromotionRequest;

import java.util.List;
import java.util.UUID;

public interface PromotionIngestService {
    UUID upsert(PromotionRequest request);
    List<UUID> upsertBatch(List<PromotionRequest> requests);
}
