package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.ProductIngestService;
import com.reviq.management.api.ingest.dto.ProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/products")
@RequiredArgsConstructor
public class ProductIngestController {

    private final ProductIngestService productIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody ProductRequest request) {
        return productIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<ProductRequest> requests) {
        return productIngestService.upsertBatch(requests);
    }
}
