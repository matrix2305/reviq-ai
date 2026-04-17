package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.TransactionIngestService;
import com.reviq.management.api.ingest.dto.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/transactions")
@RequiredArgsConstructor
public class TransactionIngestController {

    private final TransactionIngestService transactionIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody TransactionRequest request) {
        return transactionIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<TransactionRequest> requests) {
        return transactionIngestService.upsertBatch(requests);
    }
}
