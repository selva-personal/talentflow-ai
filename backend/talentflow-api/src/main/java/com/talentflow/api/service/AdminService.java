package com.talentflow.api.service;

import com.talentflow.api.ai.AiStatusService;
import com.talentflow.api.config.TalentflowProperties;
import com.talentflow.api.entity.*;
import com.talentflow.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final InterviewRepository interviewRepository;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationRepository notificationRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final MockInterviewSessionRepository mockInterviewSessionRepository;
    private final AiStatusService aiStatusService;
    private final TalentflowProperties talentflowProperties;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalUsers", userRepository.count());
        dashboard.put("totalResumes", resumeRepository.count());
        dashboard.put("totalInterviews", interviewRepository.count());
        dashboard.put("totalMockSessions", mockInterviewSessionRepository.count());
        dashboard.put("totalNotifications", notificationRepository.count());
        dashboard.put("recentActivity", activityLogRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 15))
                .getContent()
                .stream()
                .map(this::activityMap)
                .toList());
        return dashboard;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::userMap);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listResumes(Pageable pageable) {
        return resumeRepository.findAll(pageable).map(this::resumeMap);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listInterviews(Pageable pageable) {
        return interviewRepository.findAll(pageable).map(this::interviewMap);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listActivityLogs(Pageable pageable) {
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::activityMap);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAiStatus() {
        Map<String, Object> status = new LinkedHashMap<>(aiStatusService.getAdminStatus());
        status.put("configuredMode", talentflowProperties.getAi().getMode());
        return status;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("geminiConfigured", talentflowProperties.getGemini().getApiKey() != null
                && !talentflowProperties.getGemini().getApiKey().isBlank());
        config.put("aiMode", talentflowProperties.getAi().getMode());
        config.put("aiProvider", talentflowProperties.getAi().getProvider());
        config.put("aiStatus", aiStatusService.getAdminStatus());
        config.put("environment", System.getProperty("spring.profiles.active", "default"));
        config.put("totalResumeAnalyses", resumeAnalysisRepository.count());
        return config;
    }

    private Map<String, Object> userMap(User u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId());
        map.put("email", u.getEmail());
        map.put("firstName", u.getFirstName());
        map.put("lastName", u.getLastName());
        map.put("role", u.getRole().getName());
        map.put("enabled", u.isEnabled());
        map.put("lastLoginAt", u.getLastLoginAt() != null ? u.getLastLoginAt().toString() : null);
        map.put("createdAt", u.getCreatedAt().toString());
        return map;
    }

    private Map<String, Object> resumeMap(Resume r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("userId", r.getUser().getId());
        map.put("fileName", r.getFileName());
        map.put("createdAt", r.getCreatedAt().toString());
        return map;
    }

    private Map<String, Object> interviewMap(Interview i) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", i.getId());
        map.put("userId", i.getUser().getId());
        map.put("title", i.getTitle());
        map.put("status", i.getStatus());
        map.put("overallScore", i.getOverallScore());
        map.put("createdAt", i.getCreatedAt().toString());
        return map;
    }

    private Map<String, Object> activityMap(ActivityLog a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getId());
        map.put("userId", a.getUser().getId());
        map.put("action", a.getAction());
        map.put("entityType", a.getEntityType());
        map.put("entityId", a.getEntityId());
        map.put("createdAt", a.getCreatedAt().toString());
        return map;
    }
}
