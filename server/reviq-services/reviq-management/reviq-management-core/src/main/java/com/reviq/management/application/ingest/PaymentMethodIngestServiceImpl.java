package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.PaymentMethodIngestService;
import com.reviq.management.api.ingest.dto.PaymentMethodRequest;
import com.reviq.management.domain.transaction.entity.PaymentMethod;
import com.reviq.management.domain.transaction.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMethodIngestServiceImpl implements PaymentMethodIngestService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    @Transactional
    public UUID upsert(PaymentMethodRequest request) {
        PaymentMethod method = paymentMethodRepository.findByCode(request.getCode())
                .orElseGet(PaymentMethod::new);

        method.setCode(request.getCode());
        method.setName(request.getName());
        method.setSyncedAt(LocalDateTime.now());

        return paymentMethodRepository.save(method).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<PaymentMethodRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
