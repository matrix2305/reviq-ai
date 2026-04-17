package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.PromotionIngestService;
import com.reviq.management.api.ingest.dto.PromotionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/promotions")
@RequiredArgsConstructor
public class PromotionIngestController {

    private final PromotionIngestService promotionIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody PromotionRequest request) {
        return promotionIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<PromotionRequest> requests) {
        return promotionIngestService.upsertBatch(requests);
    }
}
