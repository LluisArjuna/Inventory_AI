package com.inventory.api.service;

import com.inventory.api.dto.*;
import com.inventory.api.models.*;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.ItemMapper;
import com.inventory.api.mapper.PhotoMapper;
import com.inventory.api.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    private final CoordinateRepository coordinateRepository;
    private final ItemMapper itemMapper;
    private final PhotoMapper photoMapper;

    @Transactional
    public ItemResponse create(CreateItemRequest request) {
        Inventory inventory = inventoryRepository.findById(request.inventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", request.inventoryId()));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        Coordinate coordinate = null;
        if (request.coordinateId() != null) {
            coordinate = coordinateRepository.findById(request.coordinateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Coordinate", "id", request.coordinateId()));
        }

        Item item = Item.builder()
                .name(request.name())
                .description(request.description())
                .year(request.year())
                .inventory(inventory)
                .category(category)
                .coordinate(coordinate)
                .build();

        item = itemRepository.save(item);
        return toResponseWithPhotos(item);
    }

    @Transactional(readOnly = true)
    public ItemResponse findById(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        return toResponseWithPhotos(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> findAll(Pageable pageable) {
        return itemRepository.findAll(pageable).map(this::toResponseWithPhotos);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> findByFilters(
            UUID categoryId, UUID inventoryId, Integer year, String name,
            BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng,
            Pageable pageable) {
        Specification<Item> spec = buildSpecification(categoryId, inventoryId, year, name, minLat, maxLat, minLng, maxLng);
        return itemRepository.findAll(spec, pageable).map(this::toResponseWithPhotos);
    }

    private Specification<Item> buildSpecification(
            UUID categoryId, UUID inventoryId, Integer year, String name,
            BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (inventoryId != null) {
                predicates.add(cb.equal(root.get("inventory").get("id"), inventoryId));
            }
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (minLat != null && maxLat != null && minLng != null && maxLng != null) {
                predicates.add(cb.between(root.get("coordinate").get("coordX"), minLat, maxLat));
                predicates.add(cb.between(root.get("coordinate").get("coordY"), minLng, maxLng));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public ItemResponse update(UUID id, UpdateItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        if (request.name() != null) {
            item.setName(request.name());
        }
        if (request.description() != null) {
            item.setDescription(request.description());
        }
        if (request.year() != null) {
            item.setYear(request.year());
        }
        if (request.inventoryId() != null) {
            Inventory inventory = inventoryRepository.findById(request.inventoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", request.inventoryId()));
            item.setInventory(inventory);
        }
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));
            item.setCategory(category);
        }
        if (request.coordinateId() != null) {
            Coordinate coordinate = coordinateRepository.findById(request.coordinateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Coordinate", "id", request.coordinateId()));
            item.setCoordinate(coordinate);
        }

        item = itemRepository.save(item);
        return toResponseWithPhotos(item);
    }

    @Transactional
    public void delete(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item", "id", id);
        }
        itemRepository.deleteById(id);
    }

    private ItemResponse toResponseWithPhotos(Item item) {
        List<PhotoResponse> photos = item.getPhotos().stream()
                .map(photoMapper::toResponse)
                .collect(Collectors.toList());

        Coordinate coordinate = item.getCoordinate();

        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getYear(),
                item.getInventory().getId(),
                item.getCategory().getId(),
                coordinate != null ? coordinate.getId() : null,
                coordinate != null ? coordinate.getCoordX() : null,
                coordinate != null ? coordinate.getCoordY() : null,
                photos
        );
    }
}