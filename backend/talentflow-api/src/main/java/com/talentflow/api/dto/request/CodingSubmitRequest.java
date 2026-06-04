package com.talentflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodingSubmitRequest {

    @NotBlank
    private String code;
}
