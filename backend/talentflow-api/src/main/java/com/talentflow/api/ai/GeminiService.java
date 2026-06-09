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
        GeminiProbeResult probe = probeLive(systemPrompt, userPrompt);
        if (probe.isSuccess()) {
            return probe.getResponseText();
        }
        if (!probe.isQuotaAvailable()) {
            throw new AiQuotaExceededException(probe.getFailureReason());
        }
        throw new AiUnavailableException(probe.getFailureReason());
    }

    /**
     * Live diagnostic probe — captures HTTP status, bodies, timing, and classification.
     */
    public GeminiProbeResult probeLive(String systemPrompt, String userPrompt) {
        String apiKey = properties.getGemini().getApiKey();
        String model = properties.getGemini().getModel();

        if (apiKey == null || apiKey.isBlank()) {
            return GeminiProbeResult.builder()
                    .success(false)
                    .httpStatus(0)
                    .model(model)
                    .geminiReachable(false)
                    .quotaAvailable(false)
                    .diagnosis("API_KEY_MISSING")
                    .failureReason("Gemini API key is not configured")
                    .build();
        }

        String url = String.format(GEMINI_URL, model, apiKey.trim());
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of(
                                "text", systemPrompt + "\n\n" + userPrompt
                        ))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 256
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        long start = System.currentTimeMillis();
        log.info("Gemini probe model={} key={}", model, ApiKeyUtils.mask(apiKey));

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            long elapsed = System.currentTimeMillis() - start;
            String responseBody = response.getBody();

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                return buildErrorResult(model, response.getStatusCode().value(), null, responseBody,
                        error.path("code").asInt(0), error.path("message").asText(""), elapsed, true);
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                return GeminiProbeResult.builder()
                        .success(false)
                        .httpStatus(response.getStatusCode().value())
                        .responseBody(truncate(responseBody))
                        .model(model)
                        .responseTimeMs(elapsed)
                        .geminiReachable(true)
                        .quotaAvailable(false)
                        .diagnosis("EMPTY_RESPONSE")
                        .failureReason("AI returned no candidates")
                        .build();
            }

            String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            log.info("Gemini probe succeeded in {}ms responseLength={}", elapsed, text.length());

            return GeminiProbeResult.builder()
                    .success(true)
                    .httpStatus(response.getStatusCode().value())
                    .responseBody(truncate(responseBody))
                    .model(model)
                    .responseText(text)
                    .responseTimeMs(elapsed)
                    .geminiReachable(true)
                    .quotaAvailable(true)
                    .diagnosis("OK")
                    .build();
        } catch (HttpStatusCodeException e) {
            long elapsed = System.currentTimeMillis() - start;
            String errorBody = e.getResponseBodyAsString();
            log.error("Gemini probe HTTP {} in {}ms key={} body={}",
                    e.getStatusCode().value(), elapsed, ApiKeyUtils.mask(apiKey), truncate(errorBody));

            Integer googleCode = null;
            String googleMessage = null;
            try {
                JsonNode root = objectMapper.readTree(errorBody);
                googleCode = root.path("error").path("code").asInt(0);
                googleMessage = root.path("error").path("message").asText(null);
            } catch (Exception ignored) {
                googleMessage = errorBody;
            }

            return buildErrorResult(model, e.getStatusCode().value(), null, errorBody,
                    googleCode != null && googleCode != 0 ? googleCode : e.getStatusCode().value(),
                    googleMessage != null ? googleMessage : e.getMessage(), elapsed, true);
        } catch (RestClientException e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("Gemini probe network error in {}ms: {}", elapsed, e.getMessage());
            return GeminiProbeResult.builder()
                    .success(false)
                    .httpStatus(0)
                    .errorBody(e.getMessage())
                    .model(model)
                    .responseTimeMs(elapsed)
                    .geminiReachable(false)
                    .quotaAvailable(false)
                    .diagnosis("NETWORK_ERROR")
                    .failureReason("Gemini network error: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("Gemini probe unexpected error", e);
            return GeminiProbeResult.builder()
                    .success(false)
                    .httpStatus(0)
                    .errorBody(e.getMessage())
                    .model(model)
                    .responseTimeMs(elapsed)
                    .geminiReachable(false)
                    .quotaAvailable(false)
                    .diagnosis("UNEXPECTED_ERROR")
                    .failureReason("Gemini unexpected error: " + e.getMessage())
                    .build();
        }
    }

    private GeminiProbeResult buildErrorResult(String model, int httpStatus, String responseBody,
                                               String errorBody, int googleCode, String googleMessage,
                                               long elapsed, boolean reachable) {
        return buildErrorResult(model, httpStatus, responseBody, errorBody, Integer.valueOf(googleCode),
                googleMessage, elapsed, reachable);
    }

    private GeminiProbeResult buildErrorResult(String model, int httpStatus, String responseBody,
                                               String errorBody, Integer googleCode, String googleMessage,
                                               long elapsed, boolean reachable) {
        String diagnosis = classifyDiagnosis(httpStatus, googleCode, googleMessage);
        boolean quotaAvailable = !"QUOTA_EXHAUSTED".equals(diagnosis);

        return GeminiProbeResult.builder()
                .success(false)
                .httpStatus(httpStatus)
                .responseBody(truncate(responseBody))
                .errorBody(truncate(errorBody))
                .model(model)
                .responseTimeMs(elapsed)
                .geminiReachable(reachable)
                .quotaAvailable(quotaAvailable)
                .diagnosis(diagnosis)
                .googleErrorCode(googleCode)
                .googleErrorMessage(googleMessage)
                .failureReason(googleMessage != null ? googleMessage : "Gemini API error HTTP " + httpStatus)
                .build();
    }

    static String classifyDiagnosis(int httpStatus, Integer googleCode, String message) {
        String lower = message != null ? message.toLowerCase() : "";
        int code = googleCode != null ? googleCode : httpStatus;

        if (httpStatus == 429 || code == 429 || lower.contains("quota") || lower.contains("resource_exhausted")) {
            return "QUOTA_EXHAUSTED";
        }
        if (httpStatus == 401 || code == 401 || lower.contains("api key not valid")) {
            return "INVALID_API_KEY";
        }
        if (httpStatus == 403 || code == 403 || lower.contains("permission")) {
            return "PERMISSION_DENIED";
        }
        if (httpStatus == 400 || code == 400) {
            return "BAD_REQUEST";
        }
        if (httpStatus == 404 || code == 404) {
            return "MODEL_NOT_FOUND";
        }
        if (httpStatus == 0) {
            return "NETWORK_ERROR";
        }
        return "API_ERROR";
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

    private static String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 2000 ? text.substring(0, 2000) + "…[truncated]" : text;
    }
}
