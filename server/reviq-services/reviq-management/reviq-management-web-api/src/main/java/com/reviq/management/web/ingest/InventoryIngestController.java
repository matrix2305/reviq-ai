package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.InventoryIngestService;
import com.reviq.management.api.ingest.dto.InventoryMovementRequest;
import com.reviq.management.api.ingest.dto.InventoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/inventory")
@RequiredArgsConstructor
public class InventoryIngestController {

    private final InventoryIngestService inventoryIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody InventoryRequest request) {
        return inventoryIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<InventoryRequest> requests) {
        return inventoryIngestService.upsertBatch(requests);
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID addMovement(@RequestBody InventoryMovementRequest request) {
        return inventoryIngestService.addMovement(request);
    }

    @PostMapping("/movements/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> addMovementBatch(@RequestBody List<InventoryMovementRequest> requests) {
        return inventoryIngestService.addMovementBatch(requests);
    }
}
