package com.talentflow.api.controller;

import com.talentflow.api.dto.response.ApiResponse;
import com.talentflow.api.security.SecurityUtils;
import com.talentflow.api.service.ResumeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume Analyzer")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> upload(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.uploadAndAnalyze(SecurityUtils.currentUserId(), file)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(resumeService.listResumes(SecurityUtils.currentUserId(), pageable)));
    }

    @GetMapping("/{resumeId}/analysis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analysis(@PathVariable Long resumeId) {
        return ResponseEntity.ok(ApiResponse.ok(resumeService.getAnalysis(SecurityUtils.currentUserId(), resumeId)));
    }
}
