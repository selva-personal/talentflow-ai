package com.talentflow.api.ai.provider;

import com.talentflow.api.config.TalentflowProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Runtime AI provider preference. Initialized from {@code AI_PROVIDER} env var;
 * admins can override without restart.
 */
@Slf4j
@Service
public class AiProviderConfigService {

    private final AtomicReference<String> preferredProvider;

    public AiProviderConfigService(TalentflowProperties properties) {
        String initial = properties.getAi().getProvider();
        if (initial == null || initial.isBlank()) {
            initial = AiProviderType.GEMINI.id();
        }
        this.preferredProvider = new AtomicReference<>(initial.toLowerCase().trim());
        log.info("AI preferred provider initialized: {}", preferredProvider.get());
    }

    public String getPreferredProvider() {
        return preferredProvider.get();
    }

    public String getEnvProvider() {
        return preferredProvider.get();
    }

    public void setPreferredProvider(String provider) {
        AiProviderType type = AiProviderType.fromId(provider)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported provider: " + provider + ". Use: gemini, openai, anthropic, ollama, fallback"));
        preferredProvider.set(type.id());
        log.info("AI preferred provider switched to: {}", type.id());
    }
}
