package com.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateItemRequest(
    @NotBlank(message = "Name is required")
    String name,

    String description,

    @NotNull(message = "Year is required")
    @Positive(message = "Year must be positive")
    Integer year,

    @NotNull(message = "inventoryId is required")
    UUID inventoryId,

    @NotNull(message = "categoryId is required")
    UUID categoryId,

    UUID coordinateId
) {}