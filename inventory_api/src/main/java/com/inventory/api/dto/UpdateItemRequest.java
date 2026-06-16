package com.inventory.api.dto;

import jakarta.validation.constraints.Positive;

public record UpdateItemRequest(
    String name,
    String description,

    @Positive(message = "Year must be positive")
    Integer year,

    java.util.UUID inventoryId,
    java.util.UUID categoryId,
    java.util.UUID coordinateId
) {}