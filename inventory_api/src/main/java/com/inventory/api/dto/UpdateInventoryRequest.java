package com.inventory.api.dto;

public record UpdateInventoryRequest(
    String name,

    String description,

    Boolean isPublic
) {}