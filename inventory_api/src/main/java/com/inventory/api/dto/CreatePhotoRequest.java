package com.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePhotoRequest(
    @NotNull(message = "itemId is required")
    UUID itemId,

    @NotBlank(message = "URL is required")
    String url,

    @NotNull(message = "Position is required")
    Integer position,

    String altText
) {}