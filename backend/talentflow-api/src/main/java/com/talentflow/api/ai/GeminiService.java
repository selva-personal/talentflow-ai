package com.talentflow.api.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentflow.api.config.TalentflowProperties;
import com.talentflow.api.exception.AiQuotaExceededException;
import com.talentflow.api.exception.AiUnavailableException;
import com.talentflow.api.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
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

    /** Low-level Gemini call — throws retryable exceptions on quota/unavailability. */
    public String callGemini(String systemPrompt, String userPrompt) {
        String apiKey = properties.getGemini().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiUnavailableException("Gemini API key is not configured");
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

        log.debug("Gemini request model={} promptLength={}", model, userPrompt.length());

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                handleGeminiErrorBody(error.path("message").asText(""), error.path("code").asInt(0));
            }
            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                throw new AiUnavailableException("AI returned no response");
            }
            String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            log.debug("Gemini response length={}", text.length());
            return text;
        } catch (AiQuotaExceededException | AiUnavailableException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            String bodyText = e.getResponseBodyAsString();
            log.error("Gemini HTTP {} — quota/retry evaluation (body length={})", e.getStatusCode().value(), bodyText.length());
            mapHttpError(e.getStatusCode().value(), bodyText);
            throw new AiUnavailableException("Gemini API error");
        } catch (RestClientException e) {
            log.error("Gemini network error", e);
            throw new AiUnavailableException("Gemini network error", e);
        } catch (Exception e) {
            log.error("Gemini unexpected error", e);
            throw new AiUnavailableException("Gemini unexpected error", e);
        }
    }

    /** @deprecated Use AiService instead */
    @Deprecated
    public String generate(String systemPrompt, String userPrompt) {
        return callGemini(systemPrompt, userPrompt);
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

    private void mapHttpError(int status, String body) {
        String lower = body != null ? body.toLowerCase() : "";
        if (status == 429 || lower.contains("resource_exhausted") || lower.contains("quota exceeded")
                || lower.contains("quota_exceeded")) {
            throw new AiQuotaExceededException("Gemini quota exceeded");
        }
        if (status == 503 || status == 504 || status == 502) {
            throw new AiUnavailableException("Gemini temporarily unavailable");
        }
        if (status == 401 || status == 403 || status == 400) {
            throw new AiUnavailableException("Gemini authentication or request error");
        }
        throw new AiUnavailableException("Gemini API error HTTP " + status);
    }

    private void handleGeminiErrorBody(String message, int code) {
        String lower = message != null ? message.toLowerCase() : "";
        if (code == 429 || lower.contains("resource_exhausted") || lower.contains("quota")) {
            throw new AiQuotaExceededException("Gemini quota exceeded");
        }
        throw new AiUnavailableException(message != null ? message : "Gemini API error");
    }
}
