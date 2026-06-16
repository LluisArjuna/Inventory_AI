package com.inventory.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CoordinateResponse(
    UUID id,
    BigDecimal coordX,
    BigDecimal coordY
) {}