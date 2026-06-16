package com.inventory.api.service;

import com.inventory.api.dto.AvailabilityResponse;
import com.inventory.api.dto.UpsertAvailabilityRequest;
import com.inventory.api.models.Availability;
import com.inventory.api.models.Inventory;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.exception.UnauthorizedException;
import com.inventory.api.repository.AvailabilityRepository;
import com.inventory.api.repository.InventoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailabilities(UUID inventoryId) {
        return availabilityRepository.findByInventoryId(inventoryId).stream()
                .map(a -> new AvailabilityResponse(a.getId(), a.getStartDate(), a.getEndDate()))
                .toList();
    }

    @Transactional
    public List<AvailabilityResponse> setAvailabilities(UUID inventoryId, @Valid UpsertAvailabilityRequest request, UUID userId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));

        if (!inventory.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not own this inventory");
        }

        availabilityRepository.deleteByInventoryId(inventoryId);

        List<Availability> availabilities = request.availabilities().stream()
                .map(range -> Availability.builder()
                        .inventory(inventory)
                        .startDate(range.startDate())
                        .endDate(range.endDate())
                        .build())
                .toList();

        return availabilityRepository.saveAll(availabilities).stream()
                .map(a -> new AvailabilityResponse(a.getId(), a.getStartDate(), a.getEndDate()))
                .toList();
    }
}
