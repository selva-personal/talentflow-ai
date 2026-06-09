package com.talentflow.api.ai.provider;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiProviderResult {

    private final String provider;
    private final boolean success;
    private final int httpStatus;
    private final String responseBody;
    private final String errorBody;
    private final String model;
    private final String responseText;
    private final long responseTimeMs;
    private final String diagnosis;
    private final Integer errorCode;
    private final String errorMessage;
    private final boolean reachable;
    private final boolean quotaAvailable;
    private final String failureReason;

    public boolean shouldFailover() {
        if (success) {
            return false;
        }
        return "QUOTA_EXHAUSTED".equals(diagnosis)
                || "API_ERROR".equals(diagnosis)
                || "NETWORK_ERROR".equals(diagnosis)
                || "INVALID_API_KEY".equals(diagnosis)
                || "PERMISSION_DENIED".equals(diagnosis)
                || "BAD_REQUEST".equals(diagnosis)
                || "MODEL_NOT_FOUND".equals(diagnosis)
                || "NOT_CONFIGURED".equals(diagnosis)
                || "API_KEY_MISSING".equals(diagnosis);
    }
}
