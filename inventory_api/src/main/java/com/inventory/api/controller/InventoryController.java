package com.inventory.api.controller;

import com.inventory.api.dto.*;
import com.inventory.api.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Tag(name = "Inventories", description = "Inventory management endpoints")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Create new inventory")
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody CreateInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(request));
    }

    @GetMapping("/public")
    @Operation(summary = "Get all public inventories")
    public ResponseEntity<Page<InventoryResponse>> getPublic(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 20) Pageable pageable) {
        if (name != null || email != null) {
            return ResponseEntity.ok(inventoryService.findPublicByEmail(name, email, pageable));
        }
        return ResponseEntity.ok(inventoryService.findPublic(null, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory by ID")
    public ResponseEntity<InventoryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Get all inventories with pagination")
    public ResponseEntity<Page<InventoryResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.findAll(pageable));
    }

    @GetMapping("/user/{firebaseUid}")
    @Operation(summary = "Get inventories by Firebase UID")
    public ResponseEntity<Page<InventoryResponse>> getByFirebaseUid(
            @PathVariable String firebaseUid,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.findByFirebaseUid(firebaseUid, name, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update inventory")
    public ResponseEntity<InventoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(inventoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete inventory")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}