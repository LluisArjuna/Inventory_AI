package com.inventory.api.ai.mapper;

import com.inventory.api.ai.dto.AiSuggestionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItemSuggestionNormalizer {

    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final String DEFAULT_NAME = "Item from photo";

    public AiSuggestionResponse normalize(String name, String description, Integer year, String categoryName, List<String> validCategoryNames) {
        if (name == null || name.isBlank()) {
            name = DEFAULT_NAME;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH);
        }

        if (description == null) {
            description = "";
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            description = description.substring(0, MAX_DESCRIPTION_LENGTH);
        }

        if (year == null) {
            year = 0;
        }

        String resolvedCategory = null;
        if (categoryName != null) {
            String catName = categoryName;
            boolean match = validCategoryNames.stream()
                    .anyMatch(cn -> cn.equalsIgnoreCase(catName));
            if (match) {
                resolvedCategory = categoryName;
            }
        }

        return new AiSuggestionResponse(name, description, year, resolvedCategory);
    }

    public String removeMarkdownFences(String raw) {
        if (raw == null) {
            return null;
        }

        String sanitized = raw.trim();
        if (!sanitized.startsWith("```")) {
            return sanitized;
        }

        int firstBreak = sanitized.indexOf('\n');
        if (firstBreak > -1) {
            sanitized = sanitized.substring(firstBreak + 1);
        }
        if (sanitized.endsWith("```")) {
            sanitized = sanitized.substring(0, sanitized.length() - 3);
        }
        return sanitized.trim();
    }
}
