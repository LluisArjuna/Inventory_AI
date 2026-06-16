package com.inventory.api.dto;

public record UpdatePhotoRequest(
    String url,
    Integer position,
    String altText
) {}