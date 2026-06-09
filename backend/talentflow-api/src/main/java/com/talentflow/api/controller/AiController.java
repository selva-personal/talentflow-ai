package com.talentflow.api.controller;

import com.talentflow.api.ai.AiDiagnosticsService;
import com.talentflow.api.ai.AiStatusService;
import com.talentflow.api.dto.request.AiTestRequest;
import com.talentflow.api.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final AiDiagnosticsService aiDiagnosticsService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.ok(aiStatusService.getPublicStatus()));
    }

    @GetMapping("/diagnostics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> diagnostics() {
        return ResponseEntity.ok(ApiResponse.ok(aiDiagnosticsService.getDiagnostics()));
    }

    @PostMapping("/diagnostics/probe")
    public ResponseEntity<ApiResponse<Map<String, Object>>> probeAll() {
        return ResponseEntity.ok(ApiResponse.ok(
                aiDiagnosticsService.probeAllProviders("Reply with exactly: OK")));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> test(@Valid @RequestBody AiTestRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(aiDiagnosticsService.runTest(request.getPrompt())));
    }
}
