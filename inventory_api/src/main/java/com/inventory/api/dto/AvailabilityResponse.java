package com.inventory.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AvailabilityResponse(
    UUID id,
    LocalDate startDate,
    LocalDate endDate
) {}
