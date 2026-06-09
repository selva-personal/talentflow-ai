package com.talentflow.api.ai.provider;

import com.talentflow.api.ai.AiStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiProviderOrchestrator {

    private final AiProviderRegistry registry;
    private final AiProviderConfigService configService;
    private final AiStatusService statusService;

    public AiProviderResult generateWithFailover(String systemPrompt, String userPrompt) {
        statusService.recordRequestAttempt();

        String preferred = configService.getPreferredProvider();
        if (AiProviderType.FALLBACK.id().equals(preferred)) {
            statusService.recordForcedFallback();
            return null;
        }

        List<String> chain = buildFailoverChain(preferred);
        AiProviderResult lastFailure = null;

        for (String providerName : chain) {
            AiProvider provider = registry.get(providerName).orElse(null);
            if (provider == null) {
                continue;
            }
            if (!provider.isConfigured()) {
                log.debug("Skipping unconfigured provider: {}", providerName);
                AiProviderResult skipped = AiProviderResult.builder()
                        .provider(providerName)
                        .success(false)
                        .model(provider.getModel())
                        .diagnosis("NOT_CONFIGURED")
                        .failureReason(providerName + " is not configured")
                        .reachable(false)
                        .quotaAvailable(false)
                        .build();
                statusService.recordProviderResult(providerName, skipped);
                lastFailure = skipped;
                continue;
            }

            log.debug("AI request via provider: {}", providerName);
            AiProviderResult result = provider.generate(systemPrompt, userPrompt);
            statusService.recordProviderResult(providerName, result);

            if (result.isSuccess()) {
                statusService.setActiveProvider(providerName);
                log.info("AI success via provider {} in {}ms", providerName, result.getResponseTimeMs());
                return result;
            }

            lastFailure = result;
            if (result.shouldFailover()) {
                log.warn("Provider {} failed ({}), failing over...", providerName, result.getDiagnosis());
                continue;
            }
            break;
        }

        if (lastFailure != null) {
            statusService.recordFailoverExhausted(lastFailure);
        }
        return null;
    }

    public AiProviderResult probeProvider(String providerName, String prompt) {
        AiProvider provider = registry.require(providerName);
        AiProviderResult result = provider.probe(prompt);
        statusService.recordProviderResult(providerName, result);
        if (result.isSuccess() && !AiProviderType.FALLBACK.id().equals(providerName)) {
            statusService.setActiveProvider(providerName);
        }
        return result;
    }

    public List<AiProviderResult> probeAll(String prompt) {
        List<AiProviderResult> results = new ArrayList<>();
        for (AiProviderType type : AiProviderType.FAILOVER_CHAIN) {
            registry.get(type.id()).ifPresent(p -> results.add(p.probe(prompt)));
        }
        return results;
    }

    private List<String> buildFailoverChain(String startProvider) {
        Set<String> chain = new LinkedHashSet<>();
        AiProviderType.fromId(startProvider).ifPresent(t -> chain.add(t.id()));
        for (AiProviderType type : AiProviderType.FAILOVER_CHAIN) {
            chain.add(type.id());
        }
        chain.remove(AiProviderType.FALLBACK.id());
        return List.copyOf(chain);
    }
}
