package com.talentflow.api.service;

import com.talentflow.api.ai.GeminiService;
import com.talentflow.api.entity.ActivityLog;
import com.talentflow.api.entity.ResumeAnalysis;
import com.talentflow.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final InterviewRepository interviewRepository;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationRepository notificationRepository;
    private final GeminiService geminiService;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(Long userId) {
        Double avgAts = resumeAnalysisRepository.averageAtsScoreByUserId(userId);
        Double avgInterview = interviewRepository.averageScoreByUserId(userId);
        List<ResumeAnalysis> recentAnalyses = resumeAnalysisRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        List<ActivityLog> activities = activityLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10))
                .getContent();

        List<Map<String, Object>> atsHistory = recentAnalyses.stream()
                .map(a -> Map.<String, Object>of(
                        "date", a.getCreatedAt().toString(),
                        "score", a.getAtsScore()))
                .toList();

        Set<String> skills = new LinkedHashSet<>();
        for (ResumeAnalysis a : recentAnalyses) {
            if (a.getMissingSkills() != null) skills.addAll(a.getMissingSkills());
        }
        List<Map<String, Object>> skillRadar = skills.stream().limit(8)
                .map(s -> Map.<String, Object>of("skill", s, "value", 50 + new Random().nextInt(40)))
                .toList();

        String recommendations;
        try {
            recommendations = geminiService.generate(
                    "Give 3 brief career recommendations as a JSON array of strings only.",
                    "ATS avg: " + (avgAts != null ? avgAts : "N/A") + ", Interview avg: " + (avgInterview != null ? avgInterview : "N/A"));
        } catch (Exception e) {
            recommendations = "[\"Practice mock interviews weekly\",\"Optimize resume keywords\",\"Build portfolio projects\"]";
        }

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("avgAtsScore", avgAts != null ? Math.round(avgAts) : 0);
        dashboard.put("avgInterviewScore", avgInterview != null ? Math.round(avgInterview) : 0);
        dashboard.put("unreadNotifications", notificationRepository.countByUserIdAndReadFalse(userId));
        dashboard.put("atsHistory", atsHistory);
        dashboard.put("skillRadar", skillRadar);
        dashboard.put("aiRecommendations", recommendations);
        dashboard.put("recentActivity", activities.stream().map(a -> Map.of(
                "action", a.getAction(),
                "entityType", a.getEntityType() != null ? a.getEntityType() : "",
                "createdAt", a.getCreatedAt().toString())).toList());
        return dashboard;
    }
}
