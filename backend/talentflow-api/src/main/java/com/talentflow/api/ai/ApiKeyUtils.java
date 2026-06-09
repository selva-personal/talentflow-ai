package com.talentflow.api.ai;

public final class ApiKeyUtils {

    private ApiKeyUtils() {}

    public static String mask(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        String trimmed = apiKey.trim();
        if (trimmed.length() <= 8) {
            return "****";
        }
        return trimmed.substring(0, 4) + "..." + trimmed.substring(trimmed.length() - 4);
    }

    public static String describeFormat(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "MISSING";
        }
        String trimmed = apiKey.trim();
        if (trimmed.startsWith("AIza")) {
            return "AI_STUDIO_STANDARD";
        }
        if (trimmed.startsWith("AQ.")) {
            return "GOOGLE_CLOUD_API_KEY";
        }
        return "UNKNOWN_FORMAT";
    }
}
