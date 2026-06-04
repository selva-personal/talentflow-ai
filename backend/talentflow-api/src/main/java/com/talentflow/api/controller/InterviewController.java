package com.talentflow.api.controller;

import com.talentflow.api.dto.request.GenerateInterviewRequest;
import com.talentflow.api.dto.request.SubmitAnswerRequest;
import com.talentflow.api.dto.response.ApiResponse;
import com.talentflow.api.security.SecurityUtils;
import com.talentflow.api.service.InterviewService;
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
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "AI Interview")
@SecurityRequirement(name = "bearerAuth")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generate(@Valid @RequestBody GenerateInterviewRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(interviewService.generate(SecurityUtils.currentUserId(), request)));
    }

    @PostMapping("/questions/{questionId}/answer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> answer(
            @PathVariable Long questionId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.submitAnswer(SecurityUtils.currentUserId(), questionId, request)));
    }

    @PostMapping("/{interviewId}/complete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> complete(@PathVariable Long interviewId) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.completeInterview(SecurityUtils.currentUserId(), interviewId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(interviewService.list(SecurityUtils.currentUserId(), pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(interviewService.getDetail(SecurityUtils.currentUserId(), id)));
    }
}
