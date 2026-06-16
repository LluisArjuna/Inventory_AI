package com.inventory.api.dto;

import jakarta.validation.constraints.NotNull;

public record CreateCoordinateRequest(
    @NotNull(message = "coordX is required")
    java.math.BigDecimal coordX,

    @NotNull(message = "coordY is required")
    java.math.BigDecimal coordY
) {}