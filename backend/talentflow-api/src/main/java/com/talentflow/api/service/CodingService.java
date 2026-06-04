package com.talentflow.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.talentflow.api.ai.GeminiService;
import com.talentflow.api.dto.request.CodingSubmitRequest;
import com.talentflow.api.dto.request.CodingTestRequest;
import com.talentflow.api.entity.CodingSubmission;
import com.talentflow.api.entity.CodingTest;
import com.talentflow.api.entity.User;
import com.talentflow.api.exception.ResourceNotFoundException;
import com.talentflow.api.repository.CodingSubmissionRepository;
import com.talentflow.api.repository.CodingTestRepository;
import com.talentflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CodingService {

    private final CodingTestRepository testRepository;
    private final CodingSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ActivityLogService activityLogService;

    @Transactional
    public Map<String, Object> generateTest(Long userId, CodingTestRequest req) {
        String system = """
                Generate a coding challenge as JSON:
                {"title":"...","problemStatement":"...","starterCode":"..."}
                """;
        String prompt = "Language: " + req.getLanguage() + ", Difficulty: " + req.getDifficulty()
                + ", Topic: " + (req.getTopic() != null ? req.getTopic() : "algorithms");
        String aiText = geminiService.generate(system, prompt);
        JsonNode json = geminiService.parseJsonResponse(aiText);

        User user = userRepository.getReferenceById(userId);
        CodingTest test = CodingTest.builder()
                .user(user)
                .title(json.path("title").asText("Coding Challenge"))
                .language(req.getLanguage())
                .problemStatement(json.path("problemStatement").asText())
                .starterCode(json.path("starterCode").asText(""))
                .difficulty(req.getDifficulty())
                .build();
        test = testRepository.save(test);
        activityLogService.log(userId, "CODING_TEST_CREATED", "CODING_TEST", test.getId(), null);

        return Map.of(
                "testId", test.getId(),
                "title", test.getTitle(),
                "language", test.getLanguage(),
                "problemStatement", test.getProblemStatement(),
                "starterCode", test.getStarterCode(),
                "difficulty", test.getDifficulty());
    }

    @Transactional
    public Map<String, Object> submit(Long userId, Long testId, CodingSubmitRequest req) {
        CodingTest test = getOwnedTest(userId, testId);
        String system = """
                Review code submission. JSON only:
                {
                  "passed": true/false,
                  "output": "simulated output",
                  "aiScore": 0-100,
                  "complexityAnalysis": "Big O analysis",
                  "suggestions": ["..."]
                }
                """;
        String prompt = "Language: " + test.getLanguage() + "\nProblem:\n" + test.getProblemStatement()
                + "\n\nCode:\n" + req.getCode();
        String aiText = geminiService.generate(system, prompt);
        JsonNode json = geminiService.parseJsonResponse(aiText);

        User user = userRepository.getReferenceById(userId);
        List<String> suggestions = new ArrayList<>();
        if (json.path("suggestions").isArray()) {
            json.path("suggestions").forEach(n -> suggestions.add(n.asText()));
        }

        CodingSubmission submission = CodingSubmission.builder()
                .test(test)
                .user(user)
                .code(req.getCode())
                .output(json.path("output").asText())
                .passed(json.path("passed").asBoolean(false))
                .aiScore(json.path("aiScore").asInt(0))
                .complexityAnalysis(json.path("complexityAnalysis").asText())
                .suggestions(suggestions)
                .build();
        submission = submissionRepository.save(submission);

        return Map.of(
                "submissionId", submission.getId(),
                "passed", submission.getPassed(),
                "output", submission.getOutput(),
                "aiScore", submission.getAiScore(),
                "complexityAnalysis", submission.getComplexityAnalysis(),
                "suggestions", submission.getSuggestions());
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listTests(Long userId, Pageable pageable) {
        return testRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "title", t.getTitle(),
                        "language", t.getLanguage(),
                        "difficulty", t.getDifficulty(),
                        "createdAt", t.getCreatedAt().toString()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTest(Long userId, Long testId) {
        CodingTest test = getOwnedTest(userId, testId);
        return Map.of(
                "id", test.getId(),
                "title", test.getTitle(),
                "language", test.getLanguage(),
                "problemStatement", test.getProblemStatement(),
                "starterCode", test.getStarterCode(),
                "difficulty", test.getDifficulty());
    }

    private CodingTest getOwnedTest(Long userId, Long testId) {
        CodingTest test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));
        if (!test.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Test not found");
        }
        return test;
    }
}
