package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.PromotionIngestService;
import com.reviq.management.api.ingest.dto.PromotionItemRequest;
import com.reviq.management.api.ingest.dto.PromotionRequest;
import com.reviq.management.domain.product.entity.Promotion;
import com.reviq.management.domain.product.entity.PromotionItem;
import com.reviq.management.domain.product.repository.ProductRepository;
import com.reviq.management.domain.product.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionIngestServiceImpl implements PromotionIngestService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public UUID upsert(PromotionRequest request) {
        Promotion promotion = promotionRepository.findByExternalId(request.getExternalId())
                .orElseGet(Promotion::new);

        promotion.setExternalId(request.getExternalId());
        promotion.setValidFrom(request.getValidFrom());
        promotion.setValidTo(request.getValidTo());
        promotion.setSyncedAt(LocalDateTime.now());

        promotion.getItems().clear();

        if (request.getItems() != null) {
            for (PromotionItemRequest itemReq : request.getItems()) {
                productRepository.findByExternalId(itemReq.getProductExternalId())
                        .ifPresent(product -> {
                            PromotionItem item = PromotionItem.builder()
                                    .promotion(promotion)
                                    .product(product)
                                    .price(itemReq.getPrice())
                                    .discount(itemReq.getDiscount())
                                    .build();
                            promotion.getItems().add(item);
                        });
            }
        }

        return promotionRepository.save(promotion).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<PromotionRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
