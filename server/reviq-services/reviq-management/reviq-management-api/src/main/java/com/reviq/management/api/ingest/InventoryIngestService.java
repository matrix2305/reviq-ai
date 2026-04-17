package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.InventoryMovementRequest;
import com.reviq.management.api.ingest.dto.InventoryRequest;

import java.util.List;
import java.util.UUID;

public interface InventoryIngestService {
    UUID upsert(InventoryRequest request);
    List<UUID> upsertBatch(List<InventoryRequest> requests);
    UUID addMovement(InventoryMovementRequest request);
    List<UUID> addMovementBatch(List<InventoryMovementRequest> requests);
}
