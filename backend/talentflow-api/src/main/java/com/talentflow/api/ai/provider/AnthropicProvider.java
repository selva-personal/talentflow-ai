package com.talentflow.api.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentflow.api.config.TalentflowProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnthropicProvider implements AiProvider {

    private final TalentflowProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final AiProviderHttpSupport httpSupport;

    @Override
    public String getName() {
        return AiProviderType.ANTHROPIC.id();
    }

    @Override
    public boolean isConfigured() {
        String key = properties.getAnthropic().getApiKey();
        return key != null && !key.isBlank();
    }

    @Override
    public String getModel() {
        return properties.getAnthropic().getModel();
    }

    @Override
    public AiProviderResult generate(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            return notConfigured();
        }

        String model = getModel();
        String url = properties.getAnthropic().getBaseUrl() + "/messages";
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 4096,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userPrompt))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", properties.getAnthropic().getApiKey().trim());
        headers.set("anthropic-version", "2023-06-01");

        long start = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            long elapsed = System.currentTimeMillis() - start;
            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("content").get(0).path("text").asText();
            return AiProviderResult.builder()
                    .provider(getName())
                    .success(true)
                    .httpStatus(response.getStatusCode().value())
                    .responseBody(AiProviderHttpSupport.truncate(response.getBody()))
                    .model(model)
                    .responseText(text)
                    .responseTimeMs(elapsed)
                    .diagnosis("OK")
                    .reachable(true)
                    .quotaAvailable(true)
                    .build();
        } catch (HttpStatusCodeException e) {
            return errorResult(model, e, System.currentTimeMillis() - start);
        } catch (RestClientException e) {
            return networkError(model, e, System.currentTimeMillis() - start);
        } catch (Exception e) {
            return unexpectedError(model, e, System.currentTimeMillis() - start);
        }
    }

    private AiProviderResult errorResult(String model, HttpStatusCodeException e, long elapsed) {
        AiProviderHttpSupport.ErrorDetails err = httpSupport.parseError(e);
        String diagnosis = AiProviderHttpSupport.classifyDiagnosis(
                e.getStatusCode().value(), err.code(), err.message());
        return AiProviderResult.builder()
                .provider(getName())
                .success(false)
                .httpStatus(e.getStatusCode().value())
                .errorBody(err.body())
                .model(model)
                .responseTimeMs(elapsed)
                .diagnosis(diagnosis)
                .errorCode(err.code())
                .errorMessage(err.message())
                .reachable(true)
                .quotaAvailable(!"QUOTA_EXHAUSTED".equals(diagnosis))
                .failureReason(err.message())
                .build();
    }

    private AiProviderResult networkError(String model, RestClientException e, long elapsed) {
        return AiProviderResult.builder()
                .provider(getName())
                .success(false)
                .model(model)
                .responseTimeMs(elapsed)
                .diagnosis("NETWORK_ERROR")
                .reachable(false)
                .quotaAvailable(false)
                .failureReason(e.getMessage())
                .build();
    }

    private AiProviderResult unexpectedError(String model, Exception e, long elapsed) {
        return AiProviderResult.builder()
                .provider(getName())
                .success(false)
                .model(model)
                .responseTimeMs(elapsed)
                .diagnosis("API_ERROR")
                .reachable(false)
                .quotaAvailable(false)
                .failureReason(e.getMessage())
                .build();
    }

    private AiProviderResult notConfigured() {
        return AiProviderResult.builder()
                .provider(getName())
                .success(false)
                .model(getModel())
                .diagnosis("NOT_CONFIGURED")
                .reachable(false)
                .quotaAvailable(false)
                .failureReason("Anthropic API key is not configured")
                .build();
    }
}
