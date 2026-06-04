package com.talentflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodingTestRequest {

    @NotBlank
    private String language;

    private String difficulty = "MEDIUM";

    private String topic;
}
