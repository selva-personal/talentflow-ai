package com.talentflow.api.controller;

import com.talentflow.api.dto.request.CodingSubmitRequest;
import com.talentflow.api.dto.request.CodingTestRequest;
import com.talentflow.api.dto.response.ApiResponse;
import com.talentflow.api.security.SecurityUtils;
import com.talentflow.api.service.CodingService;
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
@RequestMapping("/api/v1/coding")
@RequiredArgsConstructor
@Tag(name = "Coding Assessment")
@SecurityRequirement(name = "bearerAuth")
public class CodingController {

    private final CodingService codingService;

    @PostMapping("/tests")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generate(@Valid @RequestBody CodingTestRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(codingService.generateTest(SecurityUtils.currentUserId(), request)));
    }

    @PostMapping("/tests/{testId}/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submit(
            @PathVariable Long testId,
            @Valid @RequestBody CodingSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(codingService.submit(SecurityUtils.currentUserId(), testId, request)));
    }

    @GetMapping("/tests")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(codingService.listTests(SecurityUtils.currentUserId(), pageable)));
    }

    @GetMapping("/tests/{testId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable Long testId) {
        return ResponseEntity.ok(ApiResponse.ok(codingService.getTest(SecurityUtils.currentUserId(), testId)));
    }
}
