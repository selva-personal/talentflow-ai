package com.talentflow.api.ai;

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

    public static <T> AiCallResult<T> gemini(T data) {
        return new AiCallResult<>(data, false, "gemini");
    }

    public static <T> AiCallResult<T> fallback(T data) {
        return new AiCallResult<>(data, true, "fallback");
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
