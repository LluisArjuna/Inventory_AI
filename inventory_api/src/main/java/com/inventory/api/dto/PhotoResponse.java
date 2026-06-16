package com.inventory.api.dto;

import java.util.UUID;

public record PhotoResponse(
    UUID id,
    UUID itemId,
    String url,
    Integer position,
    String altText
) {}