package com.inventory.api.service;

import com.inventory.api.dto.*;
import com.inventory.api.models.Inventory;
import com.inventory.api.models.Item;
import com.inventory.api.models.Photo;
import com.inventory.api.models.User;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.InventoryMapper;
import com.inventory.api.repository.AvailabilityRepository;
import com.inventory.api.repository.InventoryRepository;
import com.inventory.api.repository.ItemRepository;
import com.inventory.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final AvailabilityRepository availabilityRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public InventoryResponse create(CreateInventoryRequest request) {
        User user = userRepository.findByFirebaseUid(request.firebaseUid())
                .orElseThrow(() -> new ResourceNotFoundException("User", "firebaseUid", request.firebaseUid()));

        Inventory inventory = Inventory.builder()
                .name(request.name())
                .description(request.description())
                .isPublic(request.isPublic())
                .user(user)
                .build();

        inventory = inventoryRepository.save(inventory);
        return toResponse(inventory, null);
    }

    @Transactional(readOnly = true)
    public InventoryResponse findById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        return toResponseWithPhoto(inventory);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> findAll(Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findAll(pageable);
        return pageWithPhotos(page);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> findByUserId(UUID userId, Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findByUserId(userId, pageable);
        return pageWithPhotos(page);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> findPublic(String name, Pageable pageable) {
        Page<Inventory> page;
        if (name != null && !name.isBlank()) {
            page = inventoryRepository.findByIsPublicTrueAndNameContainingIgnoreCase(name, pageable);
        } else {
            page = inventoryRepository.findByIsPublicTrue(pageable);
        }
        return pageWithPhotos(page);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> findByFirebaseUid(String firebaseUid, String name, Pageable pageable) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("User", "firebaseUid", firebaseUid));
        Page<Inventory> page;
        if (name != null && !name.isBlank()) {
            page = inventoryRepository.findByUserIdAndNameContainingIgnoreCase(user.getId(), name, pageable);
        } else {
            page = inventoryRepository.findByUserId(user.getId(), pageable);
        }
        return pageWithPhotos(page);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> findPublicByEmail(String name, String email, Pageable pageable) {
        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
            Page<Inventory> page;
            if (name != null && !name.isBlank()) {
                page = inventoryRepository.findByUserIdAndNameContainingIgnoreCase(user.getId(), name, pageable);
            } else {
                page = inventoryRepository.findByUserId(user.getId(), pageable);
            }
            List<Inventory> publicInv = page.getContent().stream().filter(Inventory::getIsPublic).toList();
            List<InventoryResponse> responses = publicInv.stream().map(inv -> toResponse(inv, null)).toList();
            return new org.springframework.data.domain.PageImpl<>(responses, pageable, publicInv.size());
        }
        return findPublic(name, pageable);
    }

    @Transactional
    public InventoryResponse update(UUID id, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        if (request.name() != null) inventory.setName(request.name());
        if (request.description() != null) inventory.setDescription(request.description());
        if (request.isPublic() != null) inventory.setIsPublic(request.isPublic());
        inventory = inventoryRepository.save(inventory);
        return toResponse(inventory, null);
    }

    @Transactional
    public void delete(UUID id) {
        if (!inventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory", "id", id);
        }
        availabilityRepository.deleteByInventoryId(id);
        inventoryRepository.deleteById(id);
    }

    private InventoryResponse toResponse(Inventory inventory, String firstPhotoUrl) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getName(),
                inventory.getDescription(),
                inventory.getIsPublic(),
                inventory.getUser().getId(),
                inventory.getUser().getEmail(),
                firstPhotoUrl
        );
    }

    private InventoryResponse toResponseWithPhoto(Inventory inventory) {
        Map<UUID, String> photoMap = firstPhotoUrlByInventoryIds(List.of(inventory.getId()));
        return toResponse(inventory, photoMap.get(inventory.getId()));
    }

    private Page<InventoryResponse> pageWithPhotos(Page<Inventory> page) {
        List<UUID> ids = page.getContent().stream()
                .map(Inventory::getId)
                .toList();
        Map<UUID, String> photoMap = firstPhotoUrlByInventoryIds(ids);
        return page.map(inv -> toResponse(inv, photoMap.get(inv.getId())));
    }

    private Map<UUID, String> firstPhotoUrlByInventoryIds(List<UUID> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();

        List<Item> items = itemRepository.findByInventoryIdInWithPhotos(ids);

        Map<UUID, String> result = new HashMap<>();

        for (Item item : items) {
            UUID invId = item.getInventory().getId();
            if (result.containsKey(invId)) continue;

            item.getPhotos().stream()
                    .min(Comparator.comparing(Photo::getPosition))
                    .ifPresent(photo -> result.put(invId, photo.getUrl()));
        }

        return result;
    }
}
