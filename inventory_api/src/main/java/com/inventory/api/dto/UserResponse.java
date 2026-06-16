package com.inventory.api.dto;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String email
) {}