package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.LoyaltyIngestService;
import com.reviq.management.api.ingest.dto.LoyaltyCardRequest;
import com.reviq.management.api.ingest.dto.LoyaltyTierRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/loyalty")
@RequiredArgsConstructor
public class LoyaltyIngestController {

    private final LoyaltyIngestService loyaltyIngestService;

    @PostMapping("/tiers")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsertTier(@RequestBody LoyaltyTierRequest request) {
        return loyaltyIngestService.upsertTier(request);
    }

    @PostMapping("/tiers/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertTierBatch(@RequestBody List<LoyaltyTierRequest> requests) {
        return loyaltyIngestService.upsertTierBatch(requests);
    }

    @PostMapping("/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsertCard(@RequestBody LoyaltyCardRequest request) {
        return loyaltyIngestService.upsertCard(request);
    }

    @PostMapping("/cards/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertCardBatch(@RequestBody List<LoyaltyCardRequest> requests) {
        return loyaltyIngestService.upsertCardBatch(requests);
    }
}
