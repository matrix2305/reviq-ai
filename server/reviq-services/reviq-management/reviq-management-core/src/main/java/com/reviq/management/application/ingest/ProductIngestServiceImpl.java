package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.ProductIngestService;
import com.reviq.management.api.ingest.dto.ProductRequest;
import com.reviq.management.domain.product.entity.Product;
import com.reviq.management.domain.product.repository.BrandRepository;
import com.reviq.management.domain.product.repository.CategoryRepository;
import com.reviq.management.domain.product.repository.ProductRepository;
import com.reviq.shared.enums.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductIngestServiceImpl implements ProductIngestService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public UUID upsert(ProductRequest request) {
        Product product = productRepository.findByExternalId(request.getExternalId())
                .orElseGet(Product::new);

        product.setExternalId(request.getExternalId());
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setManufacturer(request.getManufacturer());
        product.setBaseCode(request.getBaseCode());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSyncedAt(LocalDateTime.now());

        if (request.getType() != null) {
            product.setType(ProductType.valueOf(request.getType()));
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getBrandCode() != null) {
            brandRepository.findByCode(request.getBrandCode())
                    .ifPresent(product::setBrand);
        }
        if (request.getCategoryCode() != null) {
            categoryRepository.findByCode(request.getCategoryCode())
                    .ifPresent(product::setCategory);
        }

        return productRepository.save(product).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<ProductRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
