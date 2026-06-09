package com.talentflow.api.ai.provider;

import com.talentflow.api.ai.FallbackAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FallbackProvider implements AiProvider {

    private final FallbackAiService fallbackAiService;

    @Override
    public String getName() {
        return AiProviderType.FALLBACK.id();
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public String getModel() {
        return "offline-rules";
    }

    @Override
    public AiProviderResult generate(String systemPrompt, String userPrompt) {
        long start = System.currentTimeMillis();
        String text = fallbackAiService.generateTestReply(userPrompt);
        return AiProviderResult.builder()
                .provider(getName())
                .success(true)
                .httpStatus(200)
                .model(getModel())
                .responseText(text)
                .responseTimeMs(System.currentTimeMillis() - start)
                .diagnosis("OFFLINE_FALLBACK")
                .reachable(true)
                .quotaAvailable(true)
                .build();
    }
}
