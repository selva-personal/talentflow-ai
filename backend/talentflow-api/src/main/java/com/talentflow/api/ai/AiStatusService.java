package com.talentflow.api.ai;

import com.talentflow.api.ai.provider.AiProviderResult;
import com.talentflow.api.ai.provider.AiProviderType;
import com.talentflow.api.config.TalentflowProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiStatusService {

    private final TalentflowProperties properties;

    private final AtomicReference<String> activeProvider = new AtomicReference<>("gemini");
    private final AtomicReference<Boolean> fallbackEnabled = new AtomicReference<>(false);
    private final AtomicReference<Instant> lastRequestAt = new AtomicReference<>();
    private final AtomicReference<Instant> lastSuccessfulRequest = new AtomicReference<>();
    private final AtomicReference<Instant> lastFailureAt = new AtomicReference<>();
    private final AtomicReference<String> lastFailureReason = new AtomicReference<>();
    private final AtomicReference<Integer> lastHttpStatus = new AtomicReference<>();
    private final AtomicReference<String> lastDiagnosis = new AtomicReference<>();
    private final AtomicInteger fallbackUsageCount = new AtomicInteger(0);
    private final Map<String, ProviderState> providerStates = new ConcurrentHashMap<>();

    @Getter
    @Setter
    public static class ProviderState {
        private String status = "UNKNOWN";
        private String quotaStatus = "UNKNOWN";
        private Long latencyMs;
        private String lastError;
        private String lastDiagnosis;
        private Integer lastHttpStatus;
        private Instant lastSuccess;
        private Instant lastProbe;
        private boolean configured;
        private boolean reachable;
        private boolean quotaAvailable;
        private String model;
    }

    public void recordRequestAttempt() {
        lastRequestAt.set(Instant.now());
    }

    public void recordProviderResult(String providerName, AiProviderResult result) {
        lastRequestAt.set(Instant.now());
        ProviderState state = providerStates.computeIfAbsent(providerName, k -> new ProviderState());
        state.setLastProbe(Instant.now());
        state.setModel(result.getModel());
        state.setLatencyMs(result.getResponseTimeMs() > 0 ? result.getResponseTimeMs() : null);
        state.setLastDiagnosis(result.getDiagnosis());
        state.setLastHttpStatus(result.getHttpStatus() > 0 ? result.getHttpStatus() : null);
        state.setReachable(result.isReachable());
        state.setQuotaAvailable(result.isQuotaAvailable());

        if (result.isSuccess()) {
            state.setStatus("ONLINE");
            state.setQuotaStatus("AVAILABLE");
            state.setLastError(null);
            state.setLastSuccess(Instant.now());
            return;
        }

        state.setStatus("OFFLINE");
        state.setQuotaStatus(result.isQuotaAvailable() ? "UNAVAILABLE" : "EXHAUSTED");
        state.setLastError(result.getFailureReason() != null ? result.getFailureReason() : result.getErrorMessage());
    }

    public void setActiveProvider(String providerName) {
        activeProvider.set(providerName);
        fallbackEnabled.set(AiProviderType.FALLBACK.id().equals(providerName));
        lastSuccessfulRequest.set(Instant.now());
        lastFailureReason.set(null);
        lastDiagnosis.set("OK");
    }

    public void recordFailoverExhausted(AiProviderResult lastFailure) {
        activeProvider.set(AiProviderType.FALLBACK.id());
        fallbackEnabled.set(true);
        lastFailureAt.set(Instant.now());
        lastFailureReason.set(lastFailure.getFailureReason());
        lastHttpStatus.set(lastFailure.getHttpStatus() > 0 ? lastFailure.getHttpStatus() : null);
        lastDiagnosis.set(lastFailure.getDiagnosis());
        fallbackUsageCount.incrementAndGet();
    }

    public void recordForcedFallback() {
        activeProvider.set(AiProviderType.FALLBACK.id());
        fallbackEnabled.set(true);
        fallbackUsageCount.incrementAndGet();
        ProviderState state = providerStates.computeIfAbsent(AiProviderType.FALLBACK.id(), k -> new ProviderState());
        state.setStatus("ONLINE");
        state.setQuotaStatus("AVAILABLE");
    }

    public void markApiKeyMissing() {
        ProviderState gemini = providerStates.computeIfAbsent(AiProviderType.GEMINI.id(), k -> new ProviderState());
        gemini.setStatus("OFFLINE");
        gemini.setQuotaStatus("UNAVAILABLE");
        gemini.setLastError("Gemini API key is not configured");
        gemini.setLastDiagnosis("API_KEY_MISSING");
        activeProvider.set(AiProviderType.FALLBACK.id());
        fallbackEnabled.set(true);
    }

    public boolean isForcedFallbackMode() {
        String mode = properties.getAi().getMode();
        return mode != null && mode.equalsIgnoreCase("fallback");
    }

    public boolean isFallbackActive() {
        return Boolean.TRUE.equals(fallbackEnabled.get())
                || AiProviderType.FALLBACK.id().equals(activeProvider.get());
    }

    public String getActiveProvider() { return activeProvider.get(); }
    public String getGeminiStatus() { return getProviderStatus(AiProviderType.GEMINI.id()); }
    public String getQuotaStatus() {
        ProviderState state = providerStates.get(activeProvider.get());
        return state != null ? state.getQuotaStatus() : "UNKNOWN";
    }
    public String getLastSuccessfulRequestIso() { return formatInstant(lastSuccessfulRequest.get()); }
    public String getLastRequestAtIso() { return formatInstant(lastRequestAt.get()); }
    public String getLastFailureAtIso() { return formatInstant(lastFailureAt.get()); }
    public String getLastFailureReasonValue() { return lastFailureReason.get(); }
    public Integer getLastHttpStatus() { return lastHttpStatus.get(); }
    public String getLastDiagnosis() { return lastDiagnosis.get(); }

    public String getProviderStatus(String providerName) {
        ProviderState state = providerStates.get(providerName);
        return state != null ? state.getStatus() : "UNKNOWN";
    }

    public void updateConfigured(String providerName, boolean configured) {
        ProviderState state = providerStates.computeIfAbsent(providerName, k -> new ProviderState());
        state.setConfigured(configured);
    }

    public Map<String, Object> getProviderDiagnostics(String providerName, boolean configured, String model) {
        ProviderState state = providerStates.getOrDefault(providerName, new ProviderState());
        Map<String, Object> diag = new LinkedHashMap<>();
        diag.put("provider", providerName);
        diag.put("status", state.getStatus());
        diag.put("quotaAvailable", state.isQuotaAvailable());
        diag.put("reachable", state.isReachable());
        diag.put("configured", configured);
        diag.put("model", model != null ? model : state.getModel());
        diag.put("latencyMs", state.getLatencyMs());
        diag.put("lastError", state.getLastError());
        diag.put("lastDiagnosis", state.getLastDiagnosis());
        diag.put("lastHttpStatus", state.getLastHttpStatus());
        diag.put("lastSuccess", formatInstant(state.getLastSuccess()));
        diag.put("lastProbe", formatInstant(state.getLastProbe()));
        return diag;
    }

    public Map<String, Object> getAdminStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("activeProvider", activeProvider.get());
        status.put("configuredProvider", properties.getAi().getProvider());
        status.put("configuredMode", properties.getAi().getMode());
        status.put("fallbackMode", isFallbackActive() ? "ENABLED" : "DISABLED");
        status.put("fallbackActive", isFallbackActive());
        status.put("fallbackUsageCount", fallbackUsageCount.get());
        status.put("geminiStatus", getGeminiStatus());
        status.put("quotaStatus", getQuotaStatus());
        status.put("lastRequestAt", formatInstant(lastRequestAt.get()));
        status.put("lastSuccessfulRequest", formatInstant(lastSuccessfulRequest.get()));
        status.put("lastFailureAt", formatInstant(lastFailureAt.get()));
        status.put("lastFailureReason", lastFailureReason.get());
        status.put("lastRequestStatus", lastHttpStatus.get());
        status.put("lastDiagnosis", lastDiagnosis.get());
        status.put("providers", getAllProviderStatuses());
        return status;
    }

    public Map<String, Object> getPublicStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("activeProvider", activeProvider.get());
        status.put("fallbackActive", isFallbackActive());
        status.put("geminiStatus", getGeminiStatus());
        status.put("quotaStatus", getQuotaStatus());
        status.put("lastSuccessfulRequest", formatInstant(lastSuccessfulRequest.get()));
        status.put("lastFailureReason", lastFailureReason.get());
        status.put("providers", getAllProviderStatuses());
        return status;
    }

    public Map<String, Object> getAllProviderStatuses() {
        Map<String, Object> all = new LinkedHashMap<>();
        for (AiProviderType type : AiProviderType.values()) {
            if (type == AiProviderType.FALLBACK) {
                continue;
            }
            ProviderState state = providerStates.getOrDefault(type.id(), new ProviderState());
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("status", state.getStatus());
            p.put("quotaStatus", state.getQuotaStatus());
            p.put("latencyMs", state.getLatencyMs());
            p.put("lastError", state.getLastError());
            p.put("reachable", state.isReachable());
            p.put("quotaAvailable", state.isQuotaAvailable());
            all.put(type.id(), p);
        }
        return all;
    }

    private static String formatInstant(Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
