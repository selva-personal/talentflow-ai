package com.talentflow.api.ai;

import com.talentflow.api.ai.provider.*;
import com.talentflow.api.config.TalentflowProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDiagnosticsService {

    private static final String STARTUP_PROBE_PROMPT = "Reply with exactly: OK";

    private final AiProviderOrchestrator orchestrator;
    private final AiProviderRegistry registry;
    private final AiProviderConfigService configService;
    private final AiStatusService aiStatusService;
    private final TalentflowProperties properties;

    public Map<String, Object> runStartupProbe() {
        log.info("Running multi-provider AI startup probe...");
        return runFailoverTest(STARTUP_PROBE_PROMPT);
    }

    public Map<String, Object> getDiagnostics() {
        Map<String, Object> diag = new LinkedHashMap<>();
        diag.put("apiKeyLoaded", isAnyProviderConfigured());
        diag.put("configuredProvider", configService.getPreferredProvider());
        diag.put("configuredMode", properties.getAi().getMode());
        diag.put("activeProvider", aiStatusService.getActiveProvider());
        diag.put("provider", aiStatusService.getActiveProvider());
        diag.put("fallbackActive", aiStatusService.isFallbackActive());
        diag.put("geminiReachable", isReachable(AiProviderType.GEMINI.id()));
        diag.put("quotaAvailable", isQuotaAvailable(AiProviderType.GEMINI.id()));
        diag.put("geminiStatus", aiStatusService.getGeminiStatus());
        diag.put("quotaStatus", aiStatusService.getQuotaStatus());
        diag.put("lastSuccessfulRequest", aiStatusService.getLastSuccessfulRequestIso());
        diag.put("lastRequestAt", aiStatusService.getLastRequestAtIso());
        diag.put("lastFailureAt", aiStatusService.getLastFailureAtIso());
        diag.put("lastFailureReason", aiStatusService.getLastFailureReasonValue());
        diag.put("lastRequestStatus", aiStatusService.getLastHttpStatus());
        diag.put("lastDiagnosis", aiStatusService.getLastDiagnosis());
        diag.put("providers", buildProviderDiagnostics());
        return diag;
    }

    public Map<String, Object> runTest(String prompt) {
        return runFailoverTest(prompt);
    }

    public Map<String, Object> probeAllProviders(String prompt) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> probes = new ArrayList<>();
        for (AiProviderType type : AiProviderType.FAILOVER_CHAIN) {
            registry.get(type.id()).ifPresent(provider -> {
                AiProviderResult probe = orchestrator.probeProvider(type.id(), prompt);
                probes.add(toProbeMap(probe));
            });
        }
        result.put("providers", probes);
        result.put("activeProvider", aiStatusService.getActiveProvider());
        return result;
    }

    private Map<String, Object> runFailoverTest(String prompt) {
        AiProviderResult result = orchestrator.generateWithFailover(
                "You are a helpful assistant. Reply briefly in plain text.", prompt);

        Map<String, Object> response = new LinkedHashMap<>();
        response.putAll(getDiagnostics());

        if (result != null && result.isSuccess()) {
            response.put("provider", result.getProvider());
            response.put("response", result.getResponseText());
            response.put("fallbackActive", false);
            response.put("responseTimeMs", result.getResponseTimeMs());
            response.put("httpStatus", result.getHttpStatus());
            response.put("diagnosis", result.getDiagnosis());
            response.put("model", result.getModel());
            response.put("probeSuccess", true);
        } else {
            registry.get(AiProviderType.FALLBACK.id()).ifPresent(fb -> {
                AiProviderResult fbResult = fb.generate(
                        "You are a helpful assistant.", prompt);
                response.put("provider", AiProviderType.FALLBACK.id());
                response.put("response", fbResult.getResponseText());
                response.put("fallbackActive", true);
                response.put("responseTimeMs", fbResult.getResponseTimeMs());
                response.put("httpStatus", result != null ? result.getHttpStatus() : null);
                response.put("diagnosis", result != null ? result.getDiagnosis() : "ALL_PROVIDERS_FAILED");
                response.put("googleErrorCode", result != null ? result.getErrorCode() : null);
                response.put("googleErrorMessage", result != null ? result.getErrorMessage() : null);
                response.put("errorBody", result != null ? result.getErrorBody() : null);
                response.put("model", result != null ? result.getModel() : null);
                response.put("probeSuccess", false);
            });
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("provider", response.get("provider"));
        meta.put("fallbackActive", response.get("fallbackActive"));
        if (Boolean.TRUE.equals(response.get("fallbackActive"))) {
            meta.put("message", response.get("googleErrorMessage") != null
                    ? response.get("googleErrorMessage") : response.get("lastFailureReason"));
        }
        response.put("aiMeta", meta);
        return response;
    }

    private List<Map<String, Object>> buildProviderDiagnostics() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AiProvider provider : registry.all()) {
            if (AiProviderType.FALLBACK.id().equals(provider.getName())) {
                continue;
            }
            list.add(aiStatusService.getProviderDiagnostics(
                    provider.getName(),
                    provider.isConfigured(),
                    provider.getModel()));
        }
        return list;
    }

    private Map<String, Object> toProbeMap(AiProviderResult probe) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("provider", probe.getProvider());
        m.put("status", probe.isSuccess() ? "ONLINE" : "OFFLINE");
        m.put("quotaAvailable", probe.isQuotaAvailable());
        m.put("reachable", probe.isReachable());
        m.put("latencyMs", probe.getResponseTimeMs());
        m.put("diagnosis", probe.getDiagnosis());
        m.put("httpStatus", probe.getHttpStatus() > 0 ? probe.getHttpStatus() : null);
        m.put("errorMessage", probe.getErrorMessage());
        m.put("model", probe.getModel());
        m.put("responseText", probe.isSuccess() ? probe.getResponseText() : null);
        return m;
    }

    private boolean isAnyProviderConfigured() {
        return registry.all().stream().anyMatch(AiProvider::isConfigured);
    }

    private boolean isReachable(String provider) {
        Map<String, Object> providers = aiStatusService.getAllProviderStatuses();
        @SuppressWarnings("unchecked")
        Map<String, Object> state = (Map<String, Object>) providers.get(provider);
        return state != null && Boolean.TRUE.equals(state.get("reachable"));
    }

    private boolean isQuotaAvailable(String provider) {
        Map<String, Object> providers = aiStatusService.getAllProviderStatuses();
        @SuppressWarnings("unchecked")
        Map<String, Object> state = (Map<String, Object>) providers.get(provider);
        return state != null && Boolean.TRUE.equals(state.get("quotaAvailable"));
    }
}
