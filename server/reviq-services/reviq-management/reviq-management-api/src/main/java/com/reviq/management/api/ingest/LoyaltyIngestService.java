package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.LoyaltyCardRequest;
import com.reviq.management.api.ingest.dto.LoyaltyTierRequest;

import java.util.List;
import java.util.UUID;

public interface LoyaltyIngestService {
    UUID upsertTier(LoyaltyTierRequest request);
    List<UUID> upsertTierBatch(List<LoyaltyTierRequest> requests);
    UUID upsertCard(LoyaltyCardRequest request);
    List<UUID> upsertCardBatch(List<LoyaltyCardRequest> requests);
}
