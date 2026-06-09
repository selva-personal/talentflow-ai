package com.talentflow.api.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.talentflow.api.ai.provider.AiProviderOrchestrator;
import com.talentflow.api.ai.provider.AiProviderResult;
import com.talentflow.api.ai.provider.AiProviderType;
import com.talentflow.api.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProviderOrchestrator orchestrator;
    private final AiStatusService aiStatusService;
    private final AiJsonParser aiJsonParser;

    public AiCallResult<String> generateText(String systemPrompt, String userPrompt, Supplier<String> fallback) {
        if (aiStatusService.isForcedFallbackMode()) {
            aiStatusService.recordForcedFallback();
            log.info("AI forced fallback mode (APP_AI_MODE=fallback)");
            return AiCallResult.fallback(fallback.get());
        }

        AiProviderResult result = orchestrator.generateWithFailover(systemPrompt, userPrompt);
        if (result != null && result.isSuccess()) {
            return AiCallResult.of(result.getResponseText(), result.getProvider());
        }

        aiStatusService.recordFailoverExhausted(result != null ? result :
                AiProviderResult.builder()
                        .provider(AiProviderType.FALLBACK.id())
                        .success(false)
                        .diagnosis("ALL_PROVIDERS_FAILED")
                        .failureReason("All configured AI providers failed")
                        .build());
        return AiCallResult.fallback(fallback.get());
    }

    public AiCallResult<JsonNode> generateJson(String systemPrompt, String userPrompt, Supplier<JsonNode> fallback) {
        AiCallResult<String> textResult = generateText(systemPrompt, userPrompt,
                () -> fallback.get().toString());
        if (textResult.isFallbackUsed()) {
            return AiCallResult.fallback(fallback.get());
        }
        try {
            JsonNode json = aiJsonParser.parse(textResult.getData());
            return AiCallResult.of(json, textResult.getProvider());
        } catch (BadRequestException e) {
            aiStatusService.recordFailoverExhausted(AiProviderResult.builder()
                    .provider(textResult.getProvider())
                    .success(false)
                    .diagnosis("JSON_PARSE_ERROR")
                    .failureReason(e.getMessage())
                    .build());
            return AiCallResult.fallback(fallback.get());
        }
    }

    public void attachAiMeta(Map<String, Object> response, AiCallResult<?> result) {
        response.put("aiMeta", result.toMeta());
    }

    public JsonNode parseJson(String text) {
        return aiJsonParser.parse(text);
    }
}
