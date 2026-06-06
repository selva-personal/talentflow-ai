package com.talentflow.api.controller;

import com.talentflow.api.dto.response.ApiResponse;
import com.talentflow.api.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboard()));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listUsers(PageRequest.of(page, size))));
    }

    @GetMapping("/resumes")
    @Operation(summary = "List all resumes")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> resumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listResumes(PageRequest.of(page, size))));
    }

    @GetMapping("/interviews")
    @Operation(summary = "List all interview sessions")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> interviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listInterviews(PageRequest.of(page, size))));
    }

    @GetMapping("/activity-logs")
    @Operation(summary = "List activity logs")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> activityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listActivityLogs(PageRequest.of(page, size))));
    }

    @GetMapping("/ai-status")
    @Operation(summary = "AI provider status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> aiStatus() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAiStatus()));
    }

    @GetMapping("/settings")
    @Operation(summary = "System configuration overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> settings() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getSystemConfig()));
    }
}
