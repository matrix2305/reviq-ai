package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.PartnerIngestService;
import com.reviq.management.api.ingest.dto.PartnerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/partners")
@RequiredArgsConstructor
public class PartnerIngestController {

    private final PartnerIngestService partnerIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody PartnerRequest request) {
        return partnerIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<PartnerRequest> requests) {
        return partnerIngestService.upsertBatch(requests);
    }
}
