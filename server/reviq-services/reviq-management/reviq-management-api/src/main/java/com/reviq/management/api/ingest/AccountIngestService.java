package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.AccountRequest;

import java.util.List;
import java.util.UUID;

public interface AccountIngestService {
    UUID upsert(AccountRequest request);
    List<UUID> upsertBatch(List<AccountRequest> requests);
}
