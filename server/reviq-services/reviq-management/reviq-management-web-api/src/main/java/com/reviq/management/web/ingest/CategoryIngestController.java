package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.CategoryIngestService;
import com.reviq.management.api.ingest.dto.CategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/categories")
@RequiredArgsConstructor
public class CategoryIngestController {

    private final CategoryIngestService categoryIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody CategoryRequest request) {
        return categoryIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<CategoryRequest> requests) {
        return categoryIngestService.upsertBatch(requests);
    }
}
