package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.PartnerRequest;

import java.util.List;
import java.util.UUID;

public interface PartnerIngestService {
    UUID upsert(PartnerRequest request);
    List<UUID> upsertBatch(List<PartnerRequest> requests);
}
