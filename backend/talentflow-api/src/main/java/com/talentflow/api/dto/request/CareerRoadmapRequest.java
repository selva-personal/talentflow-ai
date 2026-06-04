package com.talentflow.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CareerRoadmapRequest {

    @NotBlank
    private String currentRole;

    @NotBlank
    private String targetRole;

    @Min(1)
    private int timelineMonths = 12;
}
