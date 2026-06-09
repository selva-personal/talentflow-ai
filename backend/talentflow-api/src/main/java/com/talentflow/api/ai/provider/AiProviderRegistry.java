package com.talentflow.api.ai.provider;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class AiProviderRegistry {

    private final Map<String, AiProvider> providers = new LinkedHashMap<>();

    public AiProviderRegistry(
            GeminiProvider gemini,
            OpenAiProvider openai,
            AnthropicProvider anthropic,
            OllamaProvider ollama,
            FallbackProvider fallback) {
        register(gemini);
        register(openai);
        register(anthropic);
        register(ollama);
        register(fallback);
    }

    private void register(AiProvider provider) {
        providers.put(provider.getName(), provider);
    }

    public Optional<AiProvider> get(String name) {
        return Optional.ofNullable(providers.get(name));
    }

    public AiProvider require(String name) {
        return get(name).orElseThrow(() -> new IllegalArgumentException("Unknown AI provider: " + name));
    }

    public Collection<AiProvider> all() {
        return providers.values();
    }
}
