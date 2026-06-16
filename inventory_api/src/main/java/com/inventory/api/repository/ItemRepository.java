package com.inventory.api.repository;

import com.inventory.api.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID>, JpaSpecificationExecutor<Item> {
    Page<Item> findByInventoryId(UUID inventoryId, Pageable pageable);
    Page<Item> findByCategoryId(UUID categoryId, Pageable pageable);
    Page<Item> findByYear(Integer year, Pageable pageable);

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.photos WHERE i.inventory.id IN :ids")
    List<Item> findByInventoryIdInWithPhotos(@Param("ids") List<UUID> ids);
}