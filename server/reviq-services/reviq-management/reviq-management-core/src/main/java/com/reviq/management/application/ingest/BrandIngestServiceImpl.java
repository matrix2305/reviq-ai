package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.BrandIngestService;
import com.reviq.management.api.ingest.dto.BrandRequest;
import com.reviq.management.domain.product.entity.Brand;
import com.reviq.management.domain.product.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandIngestServiceImpl implements BrandIngestService {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public UUID upsert(BrandRequest request) {
        Brand brand = brandRepository.findByCode(request.getCode())
                .orElseGet(Brand::new);

        brand.setCode(request.getCode());
        brand.setName(request.getName());
        brand.setUnitOfMeasure(request.getUnitOfMeasure());

        if (request.getPrivateLabel() != null) {
            brand.setPrivateLabel(request.getPrivateLabel());
        }

        return brandRepository.save(brand).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<BrandRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
