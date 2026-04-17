package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.LocationIngestService;
import com.reviq.management.api.ingest.dto.LocationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/locations")
@RequiredArgsConstructor
public class LocationIngestController {

    private final LocationIngestService locationIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody LocationRequest request) {
        return locationIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<LocationRequest> requests) {
        return locationIngestService.upsertBatch(requests);
    }
}
