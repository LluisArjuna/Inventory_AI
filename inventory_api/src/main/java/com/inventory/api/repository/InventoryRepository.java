package com.inventory.api.repository;

import com.inventory.api.models.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Page<Inventory> findByUserId(UUID userId, Pageable pageable);
    Page<Inventory> findByIsPublicTrue(Pageable pageable);
    Page<Inventory> findByIsPublicTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Inventory> findByUserIdAndNameContainingIgnoreCase(UUID userId, String name, Pageable pageable);
}