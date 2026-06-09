package com.talentflow.api.ai.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum AiProviderType {
    GEMINI("gemini"),
    OPENAI("openai"),
    ANTHROPIC("anthropic"),
    OLLAMA("ollama"),
    FALLBACK("fallback");

    public static final List<AiProviderType> FAILOVER_CHAIN =
            List.of(GEMINI, OPENAI, ANTHROPIC, OLLAMA);

    private final String id;

    AiProviderType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<AiProviderType> fromId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(t -> t.id.equalsIgnoreCase(id.trim()))
                .findFirst();
    }
}
