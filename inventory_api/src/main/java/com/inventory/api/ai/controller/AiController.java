package com.inventory.api.ai.controller;

import com.inventory.api.ai.dto.AiSuggestionResponse;
import com.inventory.api.ai.service.ItemSuggestionService;
import com.inventory.api.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Suggestions", description = "AI-powered item creation suggestions via image analysis")
public class AiController {

    private final ItemSuggestionService suggestionService;

    public AiController(ItemSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping(value = "/item-suggestions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Suggest item fields from photos",
            description = "Analyze one or more item photos and suggest name, description, year, and category.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggestions generated successfully",
                    content = @Content(schema = @Schema(implementation = AiSuggestionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid image payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "AI suggestion response could not be processed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "AI provider unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AiSuggestionResponse> suggestItem(@RequestParam("images") List<MultipartFile> images) {
        AiSuggestionResponse suggestion = suggestionService.suggest(images);
        return ResponseEntity.ok(suggestion);
    }
}
