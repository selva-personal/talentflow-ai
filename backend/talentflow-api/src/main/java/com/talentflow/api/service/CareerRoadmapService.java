package com.talentflow.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentflow.api.ai.AiCallResult;
import com.talentflow.api.ai.AiService;
import com.talentflow.api.ai.FallbackAiService;
import com.talentflow.api.dto.request.CareerRoadmapRequest;
import com.talentflow.api.entity.CareerRoadmap;
import com.talentflow.api.entity.User;
import com.talentflow.api.exception.ResourceNotFoundException;
import com.talentflow.api.repository.CareerRoadmapRepository;
import com.talentflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CareerRoadmapService {

    private final CareerRoadmapRepository roadmapRepository;
    private final UserRepository userRepository;
    private final AiService aiService;
    private final FallbackAiService fallbackAiService;
    private final ObjectMapper objectMapper;
    private final ActivityLogService activityLogService;

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> generate(Long userId, CareerRoadmapRequest req) {
        String system = """
                Create a career roadmap as JSON:
                {
                  "skills": [{"name":"","priority":"high|medium|low"}],
                  "learningPath": [{"phase":"","topics":[],"durationWeeks":0}],
                  "projects": [{"title":"","description":""}],
                  "milestones": [{"month":1,"goal":""}]
                }
                """;
        String prompt = String.format("From %s to %s in %d months",
                req.getCurrentRole(), req.getTargetRole(), req.getTimelineMonths());
        AiCallResult<JsonNode> aiResult = aiService.generateJson(system, prompt,
                () -> fallbackAiService.generateRoadmap(req));
        JsonNode json = aiResult.getData();
        Map<String, Object> roadmapData = objectMapper.convertValue(json, Map.class);

        User user = userRepository.getReferenceById(userId);
        CareerRoadmap roadmap = CareerRoadmap.builder()
                .user(user)
                .currentRole(req.getCurrentRole())
                .targetRole(req.getTargetRole())
                .timelineMonths(req.getTimelineMonths())
                .roadmapData(roadmapData)
                .build();
        roadmap = roadmapRepository.save(roadmap);
        activityLogService.log(userId, "ROADMAP_GENERATED", "CAREER_ROADMAP", roadmap.getId(), null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", roadmap.getId());
        result.put("currentRole", roadmap.getCurrentRole());
        result.put("targetRole", roadmap.getTargetRole());
        result.put("timelineMonths", roadmap.getTimelineMonths());
        result.put("roadmapData", roadmap.getRoadmapData());
        aiService.attachAiMeta(result, aiResult);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> list(Long userId, Pageable pageable) {
        return roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "currentRole", r.getCurrentRole(),
                        "targetRole", r.getTargetRole(),
                        "timelineMonths", r.getTimelineMonths(),
                        "createdAt", r.getCreatedAt().toString()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(Long userId, Long id) {
        CareerRoadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));
        if (!roadmap.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Roadmap not found");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", roadmap.getId());
        result.put("currentRole", roadmap.getCurrentRole());
        result.put("targetRole", roadmap.getTargetRole());
        result.put("timelineMonths", roadmap.getTimelineMonths());
        result.put("roadmapData", roadmap.getRoadmapData());
        return result;
    }
}
