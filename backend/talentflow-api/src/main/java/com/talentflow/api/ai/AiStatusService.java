package com.talentflow.api.ai;

import com.talentflow.api.config.TalentflowProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiStatusService {

    private final TalentflowProperties properties;

    private final AtomicReference<String> geminiStatus = new AtomicReference<>("OFFLINE");
    private final AtomicReference<String> quotaStatus = new AtomicReference<>("UNKNOWN");
    private final AtomicReference<String> activeProvider = new AtomicReference<>("gemini");
    private final AtomicReference<Boolean> fallbackEnabled = new AtomicReference<>(false);
    private final AtomicReference<Instant> lastFailureAt = new AtomicReference<>();
    private final AtomicReference<String> lastFailureReason = new AtomicReference<>();
    private final AtomicInteger totalRetryAttempts = new AtomicInteger(0);
    private final AtomicInteger quotaFailureCount = new AtomicInteger(0);

    public void recordGeminiSuccess() {
        geminiStatus.set("ONLINE");
        quotaStatus.set("AVAILABLE");
        activeProvider.set("gemini");
        fallbackEnabled.set(false);
    }

    public void recordRetryAttempt(int attempt) {
        totalRetryAttempts.incrementAndGet();
        log.warn("Gemini retry attempt {} of 3", attempt);
    }

    public void recordGeminiFailure(Throwable error) {
        geminiStatus.set("OFFLINE");
        activeProvider.set("fallback");
        fallbackEnabled.set(true);
        lastFailureAt.set(Instant.now());
        lastFailureReason.set(error != null ? error.getMessage() : "unknown");

        if (error instanceof com.talentflow.api.exception.AiQuotaExceededException
                || (error != null && error.getMessage() != null
                && error.getMessage().toLowerCase().contains("quota"))) {
            quotaStatus.set("EXHAUSTED");
            quotaFailureCount.incrementAndGet();
            log.error("Gemini quota exhausted — switching to fallback mode");
        } else {
            quotaStatus.set("UNAVAILABLE");
            log.error("Gemini unavailable — switching to fallback mode: {}", error != null ? error.getMessage() : "unknown");
        }
    }

    public void recordForcedFallback() {
        activeProvider.set("fallback");
        fallbackEnabled.set(true);
        geminiStatus.set("OFFLINE");
    }

    public boolean isForcedFallbackMode() {
        String mode = properties.getAi().getMode();
        return mode != null && mode.equalsIgnoreCase("fallback");
    }

    public Map<String, Object> getAdminStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("geminiStatus", geminiStatus.get());
        status.put("quotaStatus", quotaStatus.get());
        status.put("fallbackMode", Boolean.TRUE.equals(fallbackEnabled.get()) ? "ENABLED" : "DISABLED");
        status.put("activeProvider", activeProvider.get());
        status.put("configuredMode", properties.getAi().getMode());
        status.put("apiKeyConfigured", properties.getGemini().getApiKey() != null
                && !properties.getGemini().getApiKey().isBlank());
        status.put("totalRetryAttempts", totalRetryAttempts.get());
        status.put("quotaFailureCount", quotaFailureCount.get());
        status.put("lastFailureAt", lastFailureAt.get() != null ? lastFailureAt.get().toString() : null);
        status.put("lastFailureReason", lastFailureReason.get());
        return status;
    }

    public Map<String, Object> getPublicStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("geminiStatus", geminiStatus.get());
        status.put("quotaStatus", quotaStatus.get());
        status.put("fallbackActive", fallbackEnabled.get());
        status.put("activeProvider", activeProvider.get());
        return status;
    }
}
