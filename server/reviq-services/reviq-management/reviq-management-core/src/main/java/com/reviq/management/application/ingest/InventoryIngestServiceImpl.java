package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.InventoryIngestService;
import com.reviq.management.api.ingest.dto.InventoryMovementRequest;
import com.reviq.management.api.ingest.dto.InventoryRequest;
import com.reviq.management.domain.inventory.entity.Inventory;
import com.reviq.management.domain.inventory.entity.InventoryMovement;
import com.reviq.management.domain.inventory.repository.InventoryMovementRepository;
import com.reviq.management.domain.inventory.repository.InventoryRepository;
import com.reviq.management.domain.location.entity.Location;
import com.reviq.management.domain.location.repository.LocationRepository;
import com.reviq.management.domain.product.entity.Product;
import com.reviq.management.domain.product.repository.ProductRepository;
import com.reviq.shared.enums.InventoryMovementType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryIngestServiceImpl implements InventoryIngestService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public UUID upsert(InventoryRequest request) {
        Location location = locationRepository.findByExternalId(request.getLocationExternalId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + request.getLocationExternalId()));
        Product product = productRepository.findByExternalId(request.getProductExternalId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductExternalId()));

        Inventory inventory = inventoryRepository.findByLocationAndProduct(location, product)
                .orElseGet(Inventory::new);

        inventory.setLocation(location);
        inventory.setProduct(product);
        inventory.setQuantity(request.getQuantity());
        inventory.setSyncedAt(LocalDateTime.now());

        return inventoryRepository.save(inventory).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<InventoryRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }

    @Override
    @Transactional
    public UUID addMovement(InventoryMovementRequest request) {
        Location location = locationRepository.findByExternalId(request.getLocationExternalId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + request.getLocationExternalId()));
        Product product = productRepository.findByExternalId(request.getProductExternalId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductExternalId()));

        InventoryMovement movement = InventoryMovement.builder()
                .location(location)
                .product(product)
                .movementType(InventoryMovementType.valueOf(request.getMovementType()))
                .quantity(request.getQuantity())
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now())
                .build();

        return inventoryMovementRepository.save(movement).getId();
    }

    @Override
    @Transactional
    public List<UUID> addMovementBatch(List<InventoryMovementRequest> requests) {
        return requests.stream().map(this::addMovement).toList();
    }
}
