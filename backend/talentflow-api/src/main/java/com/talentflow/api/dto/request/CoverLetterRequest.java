package com.talentflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CoverLetterRequest {

    @NotBlank
    private String jobTitle;

    @NotBlank
    private String companyName;

    private String tone = "PROFESSIONAL";

    private String highlights;
}
