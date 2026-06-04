package com.talentflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMockSessionRequest {

    @NotBlank
    private String title;

    private String roleTarget;
}
