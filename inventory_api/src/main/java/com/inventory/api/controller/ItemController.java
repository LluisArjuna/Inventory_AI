package com.inventory.api.controller;

import com.inventory.api.dto.*;
import com.inventory.api.service.ItemService;
import com.inventory.api.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Item management endpoints")
public class ItemController {

    private final ItemService itemService;
    private final PhotoService photoService;

    @PostMapping
    @Operation(summary = "Create new item")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    public ResponseEntity<ItemResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Get all items with pagination and filtering")
    public ResponseEntity<Page<ItemResponse>> getAll(
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filter by inventory ID") @RequestParam(required = false) UUID inventoryId,
            @Parameter(description = "Filter by year") @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by name (LIKE)") @RequestParam(required = false) String name,
            @Parameter(description = "Min latitude for coordinate bounds") @RequestParam(required = false) BigDecimal minLat,
            @Parameter(description = "Max latitude for coordinate bounds") @RequestParam(required = false) BigDecimal maxLat,
            @Parameter(description = "Min longitude for coordinate bounds") @RequestParam(required = false) BigDecimal minLng,
            @Parameter(description = "Max longitude for coordinate bounds") @RequestParam(required = false) BigDecimal maxLng,
            @PageableDefault(size = 20) Pageable pageable) {
        if (categoryId != null || inventoryId != null || year != null || name != null
                || minLat != null || maxLat != null || minLng != null || maxLng != null) {
            return ResponseEntity.ok(itemService.findByFilters(categoryId, inventoryId, year, name,
                    minLat, maxLat, minLng, maxLng, pageable));
        }
        return ResponseEntity.ok(itemService.findAll(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item")
    public ResponseEntity<ItemResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(itemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{itemId}/photos/{photoId}")
    @Operation(summary = "Delete photo from item")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable UUID itemId,
            @PathVariable UUID photoId) {
        photoService.delete(photoId);
        return ResponseEntity.noContent().build();
    }
}