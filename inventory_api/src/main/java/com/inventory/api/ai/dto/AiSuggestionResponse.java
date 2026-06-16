package com.inventory.api.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "AI suggestion response for inventory item form autocompletion")
public record AiSuggestionResponse(
        @Schema(description = "Suggested item name", example = "Vintage wooden chair")
        String name,

        @Schema(description = "Suggested item description", example = "A hand-carved wooden chair from the early 1900s with floral details.")
        String description,

        @Schema(description = "Estimated year of manufacture", example = "1920")
        Integer year,

        @Schema(description = "Best matching category name", example = "Furniture")
        String categoryName
) {}
