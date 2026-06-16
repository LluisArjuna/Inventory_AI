package com.inventory.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record UpsertAvailabilityRequest(
    @NotNull @Valid List<DateRange> availabilities
) {
    public record DateRange(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
    ) {}
}
