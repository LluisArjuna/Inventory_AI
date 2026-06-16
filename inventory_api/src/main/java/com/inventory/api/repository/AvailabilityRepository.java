package com.inventory.api.repository;

import com.inventory.api.models.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    List<Availability> findByInventoryId(UUID inventoryId);
    void deleteByInventoryId(UUID inventoryId);
}
