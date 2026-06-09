package com.talentflow.api.ai;

import com.talentflow.api.ai.provider.AiProviderType;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class AiCallResult<T> {

    private final T data;
    private final boolean fallbackUsed;
    private final String provider;

    private AiCallResult(T data, boolean fallbackUsed, String provider) {
        this.data = data;
        this.fallbackUsed = fallbackUsed;
        this.provider = provider;
    }

    public static <T> AiCallResult<T> of(T data, String provider) {
        boolean fallback = AiProviderType.FALLBACK.id().equals(provider);
        return new AiCallResult<>(data, fallback, provider);
    }

    public static <T> AiCallResult<T> gemini(T data) {
        return of(data, AiProviderType.GEMINI.id());
    }

    public static <T> AiCallResult<T> fallback(T data) {
        return new AiCallResult<>(data, true, AiProviderType.FALLBACK.id());
    }

    public Map<String, Object> toMeta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("provider", provider);
        meta.put("fallbackActive", fallbackUsed);
        if (fallbackUsed) {
            meta.put("message", "AI service temporarily unavailable. Using offline analysis.");
        }
        return meta;
    }
}
