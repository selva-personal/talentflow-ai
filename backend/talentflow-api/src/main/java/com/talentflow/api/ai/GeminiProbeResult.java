package com.talentflow.api.ai;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeminiProbeResult {

    private final boolean success;
    private final int httpStatus;
    private final String responseBody;
    private final String errorBody;
    private final String model;
    private final String responseText;
    private final long responseTimeMs;
    private final String diagnosis;
    private final Integer googleErrorCode;
    private final String googleErrorMessage;
    private final boolean geminiReachable;
    private final boolean quotaAvailable;
    private final String failureReason;
}
