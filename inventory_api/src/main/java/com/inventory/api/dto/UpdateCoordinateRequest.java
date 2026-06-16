package com.inventory.api.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCoordinateRequest(
    @NotNull(message = "coordX is required")
    java.math.BigDecimal coordX,

    @NotNull(message = "coordY is required")
    java.math.BigDecimal coordY
) {}