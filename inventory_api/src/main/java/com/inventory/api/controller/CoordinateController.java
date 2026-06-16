package com.inventory.api.controller;

import com.inventory.api.dto.*;
import com.inventory.api.service.CoordinateService;
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
@RequestMapping("/api/coordinates")
@RequiredArgsConstructor
@Tag(name = "Coordinates", description = "Coordinate management endpoints")
public class CoordinateController {

    private final CoordinateService coordinateService;

    @PostMapping
    @Operation(summary = "Create new coordinate")
    public ResponseEntity<CoordinateResponse> create(@Valid @RequestBody CreateCoordinateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coordinateService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coordinate by ID")
    public ResponseEntity<CoordinateResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(coordinateService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Get all coordinates with pagination")
    public ResponseEntity<Page<CoordinateResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(coordinateService.findAll(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update coordinate")
    public ResponseEntity<CoordinateResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCoordinateRequest request) {
        return ResponseEntity.ok(coordinateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete coordinate")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        coordinateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}