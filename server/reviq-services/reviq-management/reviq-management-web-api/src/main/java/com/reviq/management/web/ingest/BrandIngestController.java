package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.BrandIngestService;
import com.reviq.management.api.ingest.dto.BrandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/brands")
@RequiredArgsConstructor
public class BrandIngestController {

    private final BrandIngestService brandIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody BrandRequest request) {
        return brandIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<BrandRequest> requests) {
        return brandIngestService.upsertBatch(requests);
    }
}
