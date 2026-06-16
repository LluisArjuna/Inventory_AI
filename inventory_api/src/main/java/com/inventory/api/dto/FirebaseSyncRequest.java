package com.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;

public record FirebaseSyncRequest(
    @NotBlank(message = "Firebase ID token is required")
    String token
) {}
