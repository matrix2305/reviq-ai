package com.reviq.management.web.ingest;

import com.reviq.management.api.ingest.PaymentMethodIngestService;
import com.reviq.management.api.ingest.dto.PaymentMethodRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/ingest/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodIngestController {

    private final PaymentMethodIngestService paymentMethodIngestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID upsert(@RequestBody PaymentMethodRequest request) {
        return paymentMethodIngestService.upsert(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UUID> upsertBatch(@RequestBody List<PaymentMethodRequest> requests) {
        return paymentMethodIngestService.upsertBatch(requests);
    }
}
