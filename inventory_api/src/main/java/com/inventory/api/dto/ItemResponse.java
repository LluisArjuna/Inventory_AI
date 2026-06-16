package com.inventory.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ItemResponse(
    UUID id,
    String name,
    String description,
    Integer year,
    UUID inventoryId,
    UUID categoryId,
    UUID coordinateId,
    BigDecimal coordX,
    BigDecimal coordY,
    List<PhotoResponse> photos
) {}