package com.inventory.api.ai.service;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.inventory.api.ai.dto.AiSuggestionResponse;
import com.inventory.api.ai.exception.AiSuggestionException;
import com.inventory.api.ai.mapper.ItemSuggestionNormalizer;
import com.inventory.api.ai.prompt.ItemSuggestionPromptFactory;
import com.inventory.api.ai.validation.ImageFileValidator;
import com.inventory.api.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ItemSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(ItemSuggestionService.class);
    private static final Pattern NAME_PATTERN = Pattern.compile("['\"]?name['\"]?\\s*:\\s*['\"]([^'\"]*)(?:['\"]|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DESC_PATTERN = Pattern.compile("['\"]?description['\"]?\\s*:\\s*['\"]([^'\"]*)(?:['\"]|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern YEAR_PATTERN = Pattern.compile("['\"]?year['\"]?\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("['\"]?categoryName['\"]?\\s*:\\s*['\"]([^'\"]*)['\"]?", Pattern.CASE_INSENSITIVE);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ObjectMapper lenientObjectMapper;
    private final BeanOutputConverter<AiSuggestionPayload> outputConverter;
    private final CategoryRepository categoryRepository;
    private final ItemSuggestionPromptFactory promptFactory;
    private final ImageFileValidator imageFileValidator;
    private final ItemSuggestionNormalizer normalizer;

    public ItemSuggestionService(
            ChatModel chatModel,
            ObjectMapper objectMapper,
            CategoryRepository categoryRepository,
            ItemSuggestionPromptFactory promptFactory,
            ImageFileValidator imageFileValidator,
            ItemSuggestionNormalizer normalizer
    ) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.outputConverter = new BeanOutputConverter<>(AiSuggestionPayload.class, objectMapper);
        this.lenientObjectMapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .build();
        this.categoryRepository = categoryRepository;
        this.promptFactory = promptFactory;
        this.imageFileValidator = imageFileValidator;
        this.normalizer = normalizer;
    }

    public AiSuggestionResponse suggest(List<MultipartFile> images) {
        for (MultipartFile image : images) {
            imageFileValidator.validate(image);
        }

        List<String> categoryNames = categoryRepository.findAll()
                .stream()
                .map(c -> c.getName())
                .collect(Collectors.toList());

        String systemPrompt = promptFactory.buildSystemPrompt(categoryNames);

        List<Media> mediaList = images.stream().map(img -> {
            try {
                MimeType mimeType = MimeTypeUtils.parseMimeType(img.getContentType());
                return new Media(mimeType, new ByteArrayResource(img.getBytes()));
            } catch (IOException e) {
                throw new AiSuggestionException(HttpStatus.BAD_REQUEST, "AI_INVALID_IMAGE", "Image file could not be read.");
            }
        }).collect(Collectors.toList());

        Message systemMessage = new SystemMessage(systemPrompt);
        UserMessage userMessage = UserMessage.builder()
                .text("Suggest item details from these images.")
                .media(mediaList)
                .build();
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        try {
            ChatResponse chatResponse = chatModel.call(prompt);
            String rawContent = chatResponse != null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null
                    ? chatResponse.getResult().getOutput().getText()
                    : null;
            AiSuggestionPayload payload = parsePayload(rawContent);
            return normalizer.normalize(payload.name(), payload.description(), payload.year(), payload.categoryName(), categoryNames);
        } catch (AiSuggestionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw mapProviderException(ex);
        }
    }

    private AiSuggestionException mapProviderException(Exception ex) {
        Throwable rootCause = rootCause(ex);
        log.warn(
                "AI provider call failed for item suggestion endpoint: {} - {}",
                rootCause.getClass().getSimpleName(),
                rootCause.getMessage()
        );

        if (ex instanceof RestClientResponseException restEx) {
            HttpStatus status = HttpStatus.resolve(restEx.getStatusCode().value());
            if (status == HttpStatus.BAD_REQUEST || status == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
                return new AiSuggestionException(
                        HttpStatus.BAD_REQUEST,
                        "AI_UNSUPPORTED_IMAGE_TYPE",
                        "The AI provider rejected this image format. Try JPEG, PNG or WebP."
                );
            }
            if (status == HttpStatus.TOO_MANY_REQUESTS) {
                return new AiSuggestionException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "AI_PROVIDER_UNAVAILABLE",
                        "AI provider quota or rate limit exceeded. Try again later."
                );
            }
        }

        if (rootCause instanceof ResourceAccessException) {
            return new AiSuggestionException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "AI_PROVIDER_UNAVAILABLE",
                    "AI provider request timed out. Try again later."
            );
        }

        return new AiSuggestionException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI_PROVIDER_UNAVAILABLE",
                "AI provider is currently unavailable."
        );
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private AiSuggestionPayload parsePayload(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            throw new AiSuggestionException(HttpStatus.BAD_GATEWAY, "AI_SUGGESTION_FAILED", "AI suggestion response was empty.");
        }

        String sanitized = normalizer.removeMarkdownFences(rawContent);
        sanitized = extractJsonObject(sanitized);

        try {
            return outputConverter.convert(sanitized);
        } catch (RuntimeException ex) {
            log.debug("BeanOutputConverter parsing failed, trying fallback");
        }

        try {
            return objectMapper.readValue(sanitized.getBytes(StandardCharsets.UTF_8), AiSuggestionPayload.class);
        } catch (IOException ex) {
            AiSuggestionPayload treePayload = parseFromJsonTree(objectMapper, sanitized);
            if (treePayload != null) {
                return treePayload;
            }

            try {
                return lenientObjectMapper.readValue(sanitized.getBytes(StandardCharsets.UTF_8), AiSuggestionPayload.class);
            } catch (IOException ignored) {
                AiSuggestionPayload lenientTreePayload = parseFromJsonTree(lenientObjectMapper, sanitized);
                if (lenientTreePayload != null) {
                    return lenientTreePayload;
                }

                AiSuggestionPayload regexPayload = extractByRegex(sanitized);
                if (regexPayload != null) {
                    return regexPayload;
                }
                throw new AiSuggestionException(HttpStatus.BAD_GATEWAY, "AI_INVALID_PROVIDER_RESPONSE", "AI suggestion response could not be parsed.");
            }
        }
    }

    private AiSuggestionPayload parseFromJsonTree(ObjectMapper mapper, String text) {
        try {
            JsonNode root = mapper.readTree(text);
            String name = textValue(root.findValue("name"));
            String description = textValue(root.findValue("description"));
            Integer year = intValue(root.findValue("year"));
            String categoryName = textValue(root.findValue("categoryName"));
            if (name == null && description == null && year == null && categoryName == null) {
                return null;
            }
            return new AiSuggestionPayload(name, description, year, categoryName);
        } catch (IOException ex) {
            return null;
        }
    }

    private String textValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText(null);
    }

    private Integer intValue(JsonNode node) {
        if (node == null || node.isNull() || !node.isNumber()) {
            return null;
        }
        return node.asInt();
    }

    private String extractJsonObject(String text) {
        if (text == null) {
            return null;
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1).trim();
        }
        return text.trim();
    }

    private AiSuggestionPayload extractByRegex(String text) {
        Matcher nameMatcher = NAME_PATTERN.matcher(text);
        Matcher descMatcher = DESC_PATTERN.matcher(text);
        Matcher yearMatcher = YEAR_PATTERN.matcher(text);
        Matcher catMatcher = CATEGORY_PATTERN.matcher(text);

        if (!nameMatcher.find() && !descMatcher.find() && !yearMatcher.find() && !catMatcher.find()) {
            return null;
        }

        String name = nameMatcher.reset().find() ? nameMatcher.group(1) : null;
        String description = descMatcher.reset().find() ? descMatcher.group(1) : null;
        Integer year = yearMatcher.reset().find() ? Integer.parseInt(yearMatcher.group(1)) : null;
        String categoryName = catMatcher.reset().find() ? catMatcher.group(1) : null;
        return new AiSuggestionPayload(name, description, year, categoryName);
    }

    private record AiSuggestionPayload(
            String name,
            String description,
            Integer year,
            String categoryName
    ) {
    }
}
