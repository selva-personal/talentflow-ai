package com.talentflow.api.controller;

import com.talentflow.api.ai.AiStatusService;
import com.talentflow.api.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Status")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiStatusService aiStatusService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.ok(aiStatusService.getPublicStatus()));
    }
}
