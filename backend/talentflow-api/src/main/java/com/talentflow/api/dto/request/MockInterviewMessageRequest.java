package com.talentflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MockInterviewMessageRequest {

    @NotBlank
    private String message;

    private String roleTarget;
}
