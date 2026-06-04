package com.talentflow.api.controller;

import com.talentflow.api.dto.request.CoverLetterRequest;
import com.talentflow.api.dto.response.ApiResponse;
import com.talentflow.api.security.SecurityUtils;
import com.talentflow.api.service.CoverLetterService;
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
@RequestMapping("/api/v1/cover-letters")
@RequiredArgsConstructor
@Tag(name = "Cover Letter")
@SecurityRequirement(name = "bearerAuth")
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> generate(@Valid @RequestBody CoverLetterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(coverLetterService.generate(SecurityUtils.currentUserId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(coverLetterService.list(SecurityUtils.currentUserId(), pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(coverLetterService.get(SecurityUtils.currentUserId(), id)));
    }
}
