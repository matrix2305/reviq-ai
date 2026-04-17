package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.AccountIngestService;
import com.reviq.management.api.ingest.dto.AccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/accounts")
@RequiredArgsConstructor
public class AccountIngestController {

    private final AccountIngestService accountIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody AccountRequest request) {
        return accountIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<AccountRequest> requests) {
        return accountIngestService.upsertBatch(requests);
    }
}
