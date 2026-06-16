package com.inventory.api.dto;

import java.util.UUID;

public record InventoryResponse(
    UUID id,
    String name,
    String description,
    Boolean isPublic,
    UUID userId,
    String userName,
    String firstPhotoUrl
) {}