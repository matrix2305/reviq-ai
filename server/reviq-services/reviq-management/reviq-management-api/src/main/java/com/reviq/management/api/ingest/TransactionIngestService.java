package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.TransactionRequest;

import java.util.List;
import java.util.UUID;

public interface TransactionIngestService {
    UUID upsert(TransactionRequest request);
    List<UUID> upsertBatch(List<TransactionRequest> requests);
}
