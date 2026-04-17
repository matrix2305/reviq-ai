package com.reviq.management.sync.service;

import com.reviq.management.domain.product.entity.Brand;
import com.reviq.management.domain.product.entity.Category;
import com.reviq.management.domain.product.entity.Product;
import com.reviq.management.domain.product.repository.BrandRepository;
import com.reviq.management.domain.product.repository.CategoryRepository;
import com.reviq.management.domain.product.repository.ProductRepository;
import com.reviq.management.sync.adapter.ErpProductAdapter;
import com.reviq.shared.enums.ProductType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSyncService {

    private final ErpProductAdapter erpProductAdapter;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public LocationSyncService.SyncResult syncBrands() {
        log.info("Starting brand sync...");
        var rows = erpProductAdapter.fetchAllBrands();
        long processed = 0, failed = 0;

        for (Map<String, Object> row : rows) {
            try {
                String code = (String) row.get("SIFRA");
                Brand brand = brandRepository.findByCode(code)
                        .orElseGet(Brand::new);

                brand.setCode(code);
                brand.setName((String) row.get("NAZIV"));
                brand.setUnitOfMeasure((String) row.get("JEDINICAMERE"));
                brand.setPrivateLabel(row.get("PL") != null && (Boolean) row.get("PL"));

                brandRepository.save(brand);
                processed++;
            } catch (Exception e) {
                log.error("Failed to sync brand: {}", row, e);
                failed++;
            }
        }
        return new LocationSyncService.SyncResult(processed, failed);
    }

    @Transactional
    public LocationSyncService.SyncResult syncCategories() {
        log.info("Starting category sync...");
        var rows = erpProductAdapter.fetchAllProductGroups();
        long processed = 0, failed = 0;

        for (Map<String, Object> row : rows) {
            try {
                String code = (String) row.get("SIFRA");
                Category category = categoryRepository.findByCode(code)
                        .orElseGet(Category::new);

                category.setCode(code);
                category.setName((String) row.get("NAZIV"));
                categoryRepository.save(category);
                processed++;
            } catch (Exception e) {
                log.error("Failed to sync category: {}", row, e);
                failed++;
            }
        }

        // Second pass: parent relationships
        for (Map<String, Object> row : rows) {
            try {
                String code = (String) row.get("SIFRA");
                String parentCode = (String) row.get("Nadredjena");
                if (parentCode != null && !parentCode.equals(code)) {
                    categoryRepository.findByCode(code).ifPresent(category -> {
                        categoryRepository.findByCode(parentCode).ifPresent(category::setParent);
                        categoryRepository.save(category);
                    });
                }
            } catch (Exception e) {
                log.error("Failed to set category parent: {}", row, e);
            }
        }
        return new LocationSyncService.SyncResult(processed, failed);
    }

    @Transactional
    public LocationSyncService.SyncResult syncProducts() {
        log.info("Starting product sync...");
        var rows = erpProductAdapter.fetchAllProducts();
        long processed = 0, failed = 0;

        for (Map<String, Object> row : rows) {
            try {
                String externalId = String.valueOf(((Number) row.get("ID")).longValue());
                Product product = productRepository.findByExternalId(externalId)
                        .orElseGet(Product::new);

                product.setExternalId(externalId);
                product.setCode((String) row.get("SIFRA"));
                product.setName((String) row.get("NAZIV"));
                product.setType(mapProductType((String) row.get("TIP")));
                product.setManufacturer((String) row.get("PROIZVODJAC"));
                product.setBaseCode((String) row.get("OSNOVNASIFRA"));
                product.setSyncedAt(LocalDateTime.now());

                String robnaGrupa = (String) row.get("ROBNAGRUPA");
                if (robnaGrupa != null) {
                    categoryRepository.findByCode(robnaGrupa)
                            .ifPresent(product::setCategory);
                }

                String brendCode = (String) row.get("BREND");
                if (brendCode != null) {
                    brandRepository.findByCode(brendCode)
                            .ifPresent(product::setBrand);
                }

                productRepository.save(product);
                processed++;
            } catch (Exception e) {
                log.error("Failed to sync product: {}", row, e);
                failed++;
            }
        }
        return new LocationSyncService.SyncResult(processed, failed);
    }

    private ProductType mapProductType(String erpType) {
        if ("U".equals(erpType)) return ProductType.SERVICE;
        return ProductType.GOODS;
    }
}
