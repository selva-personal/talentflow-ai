package com.talentflow.api.ai.provider;

import com.talentflow.api.ai.GeminiProbeResult;
import com.talentflow.api.ai.GeminiService;
import com.talentflow.api.config.TalentflowProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeminiProvider implements AiProvider {

    private final GeminiService geminiService;
    private final TalentflowProperties properties;

    @Override
    public String getName() {
        return AiProviderType.GEMINI.id();
    }

    @Override
    public boolean isConfigured() {
        String key = properties.getGemini().getApiKey();
        return key != null && !key.isBlank();
    }

    @Override
    public String getModel() {
        return properties.getGemini().getModel();
    }

    @Override
    public AiProviderResult generate(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            return notConfigured();
        }
        return toResult(geminiService.probeLive(systemPrompt, userPrompt));
    }

    private AiProviderResult toResult(GeminiProbeResult probe) {
        return AiProviderResult.builder()
                .provider(getName())
                .success(probe.isSuccess())
                .httpStatus(probe.getHttpStatus())
                .responseBody(probe.getResponseBody())
                .errorBody(probe.getErrorBody())
                .model(probe.getModel())
                .responseText(probe.getResponseText())
                .responseTimeMs(probe.getResponseTimeMs())
                .diagnosis(probe.getDiagnosis())
                .errorCode(probe.getGoogleErrorCode())
                .errorMessage(probe.getGoogleErrorMessage())
                .reachable(probe.isGeminiReachable())
                .quotaAvailable(probe.isQuotaAvailable())
                .failureReason(probe.getFailureReason())
                .build();
    }

    private AiProviderResult notConfigured() {
        return AiProviderResult.builder()
                .provider(getName())
                .success(false)
                .model(getModel())
                .diagnosis("API_KEY_MISSING")
                .reachable(false)
                .quotaAvailable(false)
                .failureReason("Gemini API key is not configured")
                .build();
    }
}
