package com.inventory.api.ai.prompt;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItemSuggestionPromptFactory {

    public String buildSystemPrompt(List<String> categoryNames) {
        return """
                You are an assistant for an inventory management platform.
                Analyze the provided images of an item and suggest form fields for a new inventory item.

                Rules:
                - Suggest a short, clear name for the item (never empty)
                - Suggest a brief description
                - Estimate the year of manufacture or origin as a number (use 0 if unknown)
                - Choose the best matching category name from this exact list: %s
                - Do not identify people or include private information
                - Return ONLY valid JSON, no markdown, no explanations

                Format: {"name": "...", "description": "...", "year": 1900, "categoryName": "..."}
                """.formatted(String.join(", ", categoryNames));
    }
}
