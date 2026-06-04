package com.talentflow.api.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentflow.api.config.TalentflowProperties;
import com.talentflow.api.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final TalentflowProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public String generate(String systemPrompt, String userPrompt) {
        String apiKey = properties.getGemini().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Gemini API key is not configured");
        }
        String model = properties.getGemini().getModel();
        String url = String.format(GEMINI_URL, model, apiKey);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of(
                                "text", systemPrompt + "\n\n" + userPrompt
                        ))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 8192
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                throw new BadRequestException("AI returned no response");
            }
            return candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API error", e);
            throw new BadRequestException("AI service unavailable: " + e.getMessage());
        }
    }

    public JsonNode parseJsonResponse(String text) {
        String cleaned = text.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();
        try {
            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            throw new BadRequestException("Failed to parse AI JSON response");
        }
    }
}
