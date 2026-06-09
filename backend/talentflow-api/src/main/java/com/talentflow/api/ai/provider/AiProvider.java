package com.talentflow.api.ai.provider;

/**
 * Strategy interface for AI text generation providers.
 */
public interface AiProvider {

    /** Provider id: gemini, openai, anthropic, ollama, fallback */
    String getName();

    boolean isConfigured();

    String getModel();

    AiProviderResult generate(String systemPrompt, String userPrompt);

    default AiProviderResult probe(String userPrompt) {
        return generate("You are a helpful assistant. Reply briefly in plain text.", userPrompt);
    }
}
