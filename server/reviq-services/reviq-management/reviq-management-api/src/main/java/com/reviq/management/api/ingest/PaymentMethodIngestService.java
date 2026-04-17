package com.reviq.management.api.ingest;

import com.reviq.management.api.ingest.dto.PaymentMethodRequest;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodIngestService {
    UUID upsert(PaymentMethodRequest request);
    List<UUID> upsertBatch(List<PaymentMethodRequest> requests);
}
