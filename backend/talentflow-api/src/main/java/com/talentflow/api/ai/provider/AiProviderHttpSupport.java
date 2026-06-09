package com.talentflow.api.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class AiProviderHttpSupport {

    private final ObjectMapper objectMapper;

    public static String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 2000 ? text.substring(0, 2000) + "…[truncated]" : text;
    }

    public ErrorDetails parseError(HttpStatusCodeException e) {
        String body = e.getResponseBodyAsString();
        Integer code = e.getStatusCode().value();
        String message = e.getMessage();
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("error")) {
                JsonNode err = root.path("error");
                if (err.has("code")) {
                    code = err.path("code").asInt(code);
                }
                if (err.has("message")) {
                    message = err.path("message").asText(message);
                }
            }
        } catch (Exception ignored) {
            message = body != null ? body : message;
        }
        return new ErrorDetails(code, message, truncate(body));
    }

    public ErrorDetails parseError(RestClientException e) {
        return new ErrorDetails(0, e.getMessage(), e.getMessage());
    }

    public static String classifyDiagnosis(int httpStatus, Integer errorCode, String message) {
        String lower = message != null ? message.toLowerCase() : "";
        int code = errorCode != null ? errorCode : httpStatus;

        if (httpStatus == 429 || code == 429 || lower.contains("quota") || lower.contains("resource_exhausted")) {
            return "QUOTA_EXHAUSTED";
        }
        if (httpStatus == 401 || code == 401 || lower.contains("api key not valid") || lower.contains("invalid api key")) {
            return "INVALID_API_KEY";
        }
        if (httpStatus == 403 || code == 403 || lower.contains("permission")) {
            return "PERMISSION_DENIED";
        }
        if (httpStatus == 400 || code == 400) {
            return "BAD_REQUEST";
        }
        if (httpStatus == 404 || code == 404) {
            return "MODEL_NOT_FOUND";
        }
        if (httpStatus == 0) {
            return "NETWORK_ERROR";
        }
        return "API_ERROR";
    }

    public record ErrorDetails(Integer code, String message, String body) {}
}
