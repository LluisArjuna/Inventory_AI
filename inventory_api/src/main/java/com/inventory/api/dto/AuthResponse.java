package com.inventory.api.dto;

public record AuthResponse(
    String token,
    String type
) {
    public AuthResponse(String token) {
        this(token, "Bearer");
    }
}