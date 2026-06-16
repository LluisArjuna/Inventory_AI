package com.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInventoryRequest(
    @NotBlank(message = "name is required")
    String name,

    String description,

    @NotNull(message = "isPublic is required")
    Boolean isPublic,

    @NotNull(message = "firebaseUid is required")
    String firebaseUid
) {}