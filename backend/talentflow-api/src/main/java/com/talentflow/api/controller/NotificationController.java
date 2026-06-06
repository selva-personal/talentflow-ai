package com.talentflow.api.controller;

import com.talentflow.api.dto.response.ApiResponse;
import com.talentflow.api.security.SecurityUtils;
import com.talentflow.api.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List notifications")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.currentUserId();
        Page<Map<String, Object>> result = notificationService.list(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        notificationService.markRead(SecurityUtils.currentUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        notificationService.markAllRead(SecurityUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}
