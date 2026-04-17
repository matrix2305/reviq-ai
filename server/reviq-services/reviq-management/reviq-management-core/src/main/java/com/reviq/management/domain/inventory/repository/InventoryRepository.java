package com.reviq.management.domain.inventory.repository;

import com.reviq.management.domain.inventory.entity.Inventory;
import com.reviq.management.domain.location.entity.Location;
import com.reviq.management.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByLocationAndProduct(Location location, Product product);

    List<Inventory> findByLocation(Location location);

    List<Inventory> findByProduct(Product product);
}
