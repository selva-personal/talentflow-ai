package com.talentflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateInterviewRequest {

    @NotBlank
    private String roleTarget;

    @NotBlank
    private String experienceLevel;

    @NotBlank
    private String skillLevel;

    private String interviewType = "MIXED";
}
