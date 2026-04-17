package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.ContactIngestService;
import com.reviq.management.api.ingest.dto.ContactRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/contacts")
@RequiredArgsConstructor
public class ContactIngestController {

    private final ContactIngestService contactIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody ContactRequest request) {
        return contactIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<ContactRequest> requests) {
        return contactIngestService.upsertBatch(requests);
    }
}
