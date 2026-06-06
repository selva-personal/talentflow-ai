package com.talentflow.api.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.talentflow.api.config.TalentflowProperties;
import com.talentflow.api.dto.request.*;
import com.talentflow.api.exception.AiQuotaExceededException;
import com.talentflow.api.exception.AiUnavailableException;
import com.talentflow.api.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final GeminiService geminiService;
    private final AiStatusService aiStatusService;
    private final TalentflowProperties properties;
    private final RetryTemplate geminiRetryTemplate;

    public AiCallResult<String> generateText(String systemPrompt, String userPrompt, Supplier<String> fallback) {
        if (aiStatusService.isForcedFallbackMode()) {
            aiStatusService.recordForcedFallback();
            log.info("AI forced fallback mode (APP_AI_MODE=fallback)");
            return AiCallResult.fallback(fallback.get());
        }

        try {
            String text = geminiRetryTemplate.execute((RetryCallback<String, RuntimeException>) context -> {
                if (context.getRetryCount() > 0) {
                    aiStatusService.recordRetryAttempt(context.getRetryCount());
                }
                log.debug("Gemini generate attempt {}", context.getRetryCount() + 1);
                return geminiService.callGemini(systemPrompt, userPrompt);
            });
            aiStatusService.recordGeminiSuccess();
            return AiCallResult.gemini(text);
        } catch (AiQuotaExceededException | AiUnavailableException e) {
            aiStatusService.recordGeminiFailure(e);
            return AiCallResult.fallback(fallback.get());
        } catch (Exception e) {
            aiStatusService.recordGeminiFailure(e);
            log.warn("Gemini call failed, using fallback: {}", e.getMessage());
            return AiCallResult.fallback(fallback.get());
        }
    }

    public AiCallResult<JsonNode> generateJson(String systemPrompt, String userPrompt, Supplier<JsonNode> fallback) {
        AiCallResult<String> textResult = generateText(systemPrompt, userPrompt,
                () -> fallback.get().toString());
        if (textResult.isFallbackUsed()) {
            return AiCallResult.fallback(fallback.get());
        }
        try {
            JsonNode json = geminiService.parseJsonResponse(textResult.getData());
            return AiCallResult.gemini(json);
        } catch (BadRequestException e) {
            aiStatusService.recordGeminiFailure(e);
            return AiCallResult.fallback(fallback.get());
        }
    }

    public void attachAiMeta(Map<String, Object> response, AiCallResult<?> result) {
        response.put("aiMeta", result.toMeta());
    }

    public JsonNode parseJson(String text) {
        return geminiService.parseJsonResponse(text);
    }
}
