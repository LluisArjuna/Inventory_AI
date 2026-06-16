package com.inventory.api.controller;

import com.inventory.api.dto.AvailabilityResponse;
import com.inventory.api.dto.UpsertAvailabilityRequest;
import com.inventory.api.security.UserDetailsImpl;
import com.inventory.api.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventories/{inventoryId}/availabilities")
@RequiredArgsConstructor
@Tag(name = "Availabilities", description = "Inventory lending availability endpoints")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    @Operation(summary = "Get availability date ranges for an inventory")
    public ResponseEntity<List<AvailabilityResponse>> getAvailabilities(@PathVariable UUID inventoryId) {
        return ResponseEntity.ok(availabilityService.getAvailabilities(inventoryId));
    }

    @PutMapping
    @Operation(summary = "Replace all availability date ranges (owner only)")
    public ResponseEntity<List<AvailabilityResponse>> setAvailabilities(
            @PathVariable UUID inventoryId,
            @Valid @RequestBody UpsertAvailabilityRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(availabilityService.setAvailabilities(inventoryId, request, user.getId()));
    }
}
