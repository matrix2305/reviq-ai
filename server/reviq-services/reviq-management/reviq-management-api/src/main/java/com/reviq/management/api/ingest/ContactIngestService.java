package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.ContactRequest;

import java.util.List;
import java.util.UUID;

public interface ContactIngestService {
    UUID upsert(ContactRequest request);
    List<UUID> upsertBatch(List<ContactRequest> requests);
}
