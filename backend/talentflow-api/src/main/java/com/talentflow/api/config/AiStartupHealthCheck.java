package com.talentflow.api.config;

import com.talentflow.api.ai.AiDiagnosticsService;
import com.talentflow.api.ai.AiStatusService;
import com.talentflow.api.ai.provider.AiProvider;
import com.talentflow.api.ai.provider.AiProviderRegistry;
import com.talentflow.api.ai.provider.AiProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiStartupHealthCheck {

    private final AiStatusService aiStatusService;
    private final AiDiagnosticsService aiDiagnosticsService;
    private final AiProviderRegistry registry;

    @EventListener(ApplicationReadyEvent.class)
    public void probeOnStartup() {
        for (AiProvider provider : registry.all()) {
            aiStatusService.updateConfigured(provider.getName(), provider.isConfigured());
        }

        if (aiStatusService.isForcedFallbackMode()) {
            log.info("AI startup probe skipped — APP_AI_MODE=fallback");
            aiStatusService.recordForcedFallback();
            return;
        }

        boolean anyConfigured = registry.all().stream()
                .filter(p -> !AiProviderType.FALLBACK.id().equals(p.getName()))
                .anyMatch(AiProvider::isConfigured);

        if (!anyConfigured) {
            log.warn("AI startup probe skipped — no AI provider API keys configured");
            aiStatusService.markApiKeyMissing();
            return;
        }

        Map<String, Object> result = aiDiagnosticsService.runStartupProbe();
        log.info("AI startup probe: activeProvider={} diagnosis={} fallbackActive={}",
                result.get("activeProvider"), result.get("lastDiagnosis"), result.get("fallbackActive"));
    }
}
