package com.talentflow.api.controller;

import com.talentflow.api.dto.request.CreateMockSessionRequest;
import com.talentflow.api.dto.request.MockInterviewMessageRequest;
import com.talentflow.api.dto.response.ApiResponse;
import com.talentflow.api.security.SecurityUtils;
import com.talentflow.api.service.MockInterviewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/mock-interviews")
@RequiredArgsConstructor
@Tag(name = "Mock Interview")
@SecurityRequirement(name = "bearerAuth")
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@Valid @RequestBody CreateMockSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(mockInterviewService.createSession(SecurityUtils.currentUserId(), request)));
    }

    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<ApiResponse<Map<String, Object>>> message(
            @PathVariable Long sessionId,
            @Valid @RequestBody MockInterviewMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                mockInterviewService.sendMessage(SecurityUtils.currentUserId(), sessionId, request)));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(mockInterviewService.getSession(SecurityUtils.currentUserId(), sessionId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> list(
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(mockInterviewService.listSessions(SecurityUtils.currentUserId(), pageable)));
    }
}
