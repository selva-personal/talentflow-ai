package com.talentflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiProviderSwitchRequest {

    @NotBlank
    private String provider;
}
