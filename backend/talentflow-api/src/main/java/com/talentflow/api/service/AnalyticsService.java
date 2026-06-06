package com.talentflow.api.service;

import com.talentflow.api.ai.AiCallResult;
import com.talentflow.api.ai.AiService;
import com.talentflow.api.ai.AiStatusService;
import com.talentflow.api.ai.FallbackAiService;
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
    private final ResumeRepository resumeRepository;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationRepository notificationRepository;
    private final AiService aiService;
    private final FallbackAiService fallbackAiService;
    private final AiStatusService aiStatusService;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(Long userId) {
        Double avgAts = resumeAnalysisRepository.averageAtsScoreByUserId(userId);
        Double avgInterview = interviewRepository.averageScoreByUserId(userId);
        long resumeCount = resumeRepository.countByUserId(userId);
        long interviewCount = interviewRepository.countByUserId(userId);
        List<ResumeAnalysis> recentAnalyses = resumeAnalysisRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        List<ActivityLog> activities = activityLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10))
                .getContent();

        List<Map<String, Object>> atsHistory = recentAnalyses.stream()
                .map(a -> Map.<String, Object>of(
                        "date", a.getCreatedAt().toString(),
                        "score", a.getAtsScore()))
                .toList();

        List<Map<String, Object>> skillRadar = buildSkillRadar(recentAnalyses);

        AiCallResult<String> recResult = aiService.generateText(
                "Give 3 brief career recommendations as a JSON array of strings only.",
                "ATS avg: " + (avgAts != null ? avgAts : "N/A") + ", Interview avg: " + (avgInterview != null ? avgInterview : "N/A"),
                () -> fallbackAiService.careerRecommendations(avgAts, avgInterview));
        String recommendations = recResult.getData();

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("avgAtsScore", avgAts != null ? Math.round(avgAts) : 0);
        dashboard.put("avgInterviewScore", avgInterview != null ? Math.round(avgInterview) : 0);
        dashboard.put("resumeCount", resumeCount);
        dashboard.put("interviewCount", interviewCount);
        dashboard.put("accountStatus", "ACTIVE");
        dashboard.put("unreadNotifications", notificationRepository.countByUserIdAndReadFalse(userId));
        dashboard.put("atsHistory", atsHistory);
        dashboard.put("skillRadar", skillRadar);
        dashboard.put("aiRecommendations", recommendations);
        dashboard.put("recentActivity", activities.stream().map(a -> Map.of(
                "action", a.getAction(),
                "entityType", a.getEntityType() != null ? a.getEntityType() : "",
                "createdAt", a.getCreatedAt().toString())).toList());
        dashboard.put("aiStatus", aiStatusService.getPublicStatus());
        aiService.attachAiMeta(dashboard, recResult);
        return dashboard;
    }

    private List<Map<String, Object>> buildSkillRadar(List<ResumeAnalysis> analyses) {
        if (analyses.isEmpty()) {
            return List.of();
        }
        ResumeAnalysis latest = analyses.getFirst();
        List<Map<String, Object>> radar = new ArrayList<>();
        int strengthBase = Math.min(95, latest.getAtsScore() + 15);
        int gapBase = Math.max(20, 100 - latest.getAtsScore());

        if (latest.getStrengths() != null) {
            for (String skill : latest.getStrengths().stream().limit(4).toList()) {
                radar.add(Map.of("skill", skill, "value", strengthBase));
            }
        }
        if (latest.getMissingSkills() != null) {
            for (String skill : latest.getMissingSkills().stream().limit(4).toList()) {
                radar.add(Map.of("skill", skill, "value", gapBase));
            }
        }
        return radar;
    }
}
