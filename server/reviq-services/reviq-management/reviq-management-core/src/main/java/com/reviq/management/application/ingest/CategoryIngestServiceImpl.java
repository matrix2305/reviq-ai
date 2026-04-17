package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.CategoryIngestService;
import com.reviq.management.api.ingest.dto.CategoryRequest;
import com.reviq.management.domain.product.entity.Category;
import com.reviq.management.domain.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryIngestServiceImpl implements CategoryIngestService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public UUID upsert(CategoryRequest request) {
        Category category = categoryRepository.findByCode(request.getCode())
                .orElseGet(Category::new);

        category.setCode(request.getCode());
        category.setName(request.getName());

        if (request.getParentCode() != null) {
            categoryRepository.findByCode(request.getParentCode())
                    .ifPresent(category::setParent);
        }

        return categoryRepository.save(category).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<CategoryRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
