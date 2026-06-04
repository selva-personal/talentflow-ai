package com.talentflow.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.talentflow.api.ai.GeminiService;
import com.talentflow.api.dto.request.GenerateInterviewRequest;
import com.talentflow.api.dto.request.SubmitAnswerRequest;
import com.talentflow.api.entity.*;
import com.talentflow.api.exception.ResourceNotFoundException;
import com.talentflow.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository questionRepository;
    private final InterviewAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ActivityLogService activityLogService;

    @Transactional
    public Map<String, Object> generate(Long userId, GenerateInterviewRequest req) {
        String system = """
                Generate interview questions as JSON only:
                {
                  "title": "string",
                  "questions": [
                    {"type": "TECHNICAL|BEHAVIORAL|HR|SYSTEM_DESIGN", "text": "question"}
                  ]
                }
                Generate 8-12 diverse questions.
                """;
        String prompt = String.format("Role: %s, Experience: %s, Skill: %s, Type: %s",
                req.getRoleTarget(), req.getExperienceLevel(), req.getSkillLevel(), req.getInterviewType());
        String aiText = geminiService.generate(system, prompt);
        JsonNode json = geminiService.parseJsonResponse(aiText);

        User user = userRepository.getReferenceById(userId);
        Interview interview = Interview.builder()
                .user(user)
                .title(json.path("title").asText(req.getRoleTarget() + " Interview"))
                .roleTarget(req.getRoleTarget())
                .experienceLevel(req.getExperienceLevel())
                .skillLevel(req.getSkillLevel())
                .interviewType(req.getInterviewType())
                .status("READY")
                .build();
        interview = interviewRepository.save(interview);

        List<Map<String, Object>> questionDtos = new ArrayList<>();
        int order = 0;
        for (JsonNode q : json.path("questions")) {
            InterviewQuestion question = InterviewQuestion.builder()
                    .interview(interview)
                    .questionType(q.path("type").asText("TECHNICAL"))
                    .questionText(q.path("text").asText())
                    .sortOrder(order++)
                    .build();
            question = questionRepository.save(question);
            questionDtos.add(Map.of(
                    "id", question.getId(),
                    "type", question.getQuestionType(),
                    "text", question.getQuestionText()));
        }
        activityLogService.log(userId, "INTERVIEW_GENERATED", "INTERVIEW", interview.getId(), null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("interviewId", interview.getId());
        result.put("title", interview.getTitle());
        result.put("questions", questionDtos);
        return result;
    }

    @Transactional
    public Map<String, Object> submitAnswer(Long userId, Long questionId, SubmitAnswerRequest req) {
        InterviewQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        Interview interview = question.getInterview();
        if (!interview.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Question not found");
        }

        String system = """
                Evaluate the interview answer. Respond JSON only:
                {"score": 0-100, "feedback": "detailed feedback"}
                """;
        String prompt = "Question (" + question.getQuestionType() + "): " + question.getQuestionText()
                + "\n\nAnswer: " + req.getAnswerText();
        String aiText = geminiService.generate(system, prompt);
        JsonNode json = geminiService.parseJsonResponse(aiText);

        User user = userRepository.getReferenceById(userId);
        InterviewAnswer answer = InterviewAnswer.builder()
                .question(question)
                .user(user)
                .answerText(req.getAnswerText())
                .score(json.path("score").asInt(70))
                .feedback(json.path("feedback").asText())
                .build();
        answer = answerRepository.save(answer);

        return Map.of(
                "answerId", answer.getId(),
                "score", answer.getScore(),
                "feedback", answer.getFeedback());
    }

    @Transactional
    public Map<String, Object> completeInterview(Long userId, Long interviewId) {
        Interview interview = getOwnedInterview(userId, interviewId);
        List<InterviewQuestion> questions = questionRepository.findByInterviewIdOrderBySortOrderAsc(interviewId);
        List<InterviewAnswer> answers = new ArrayList<>();
        for (InterviewQuestion q : questions) {
            answerRepository.findTopByQuestionIdOrderByCreatedAtDesc(q.getId()).ifPresent(answers::add);
        }
        int avg = answers.isEmpty() ? 0 :
                (int) answers.stream().mapToInt(a -> a.getScore() != null ? a.getScore() : 0).average().orElse(0);

        String system = """
                Summarize interview performance. JSON only:
                {"feedback": "summary", "improvements": ["item1","item2"]}
                """;
        String prompt = "Role: " + interview.getRoleTarget() + ", Average score: " + avg;
        String aiText = geminiService.generate(system, prompt);
        JsonNode json = geminiService.parseJsonResponse(aiText);

        interview.setOverallScore(avg);
        interview.setFeedback(json.path("feedback").asText());
        interview.setImprovements(jsonArrayToList(json.path("improvements")));
        interview.setStatus("COMPLETED");
        interviewRepository.save(interview);

        return Map.of(
                "interviewId", interview.getId(),
                "overallScore", interview.getOverallScore(),
                "feedback", interview.getFeedback(),
                "improvements", interview.getImprovements());
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> list(Long userId, Pageable pageable) {
        return interviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(i -> Map.<String, Object>of(
                        "id", i.getId(),
                        "title", i.getTitle(),
                        "roleTarget", i.getRoleTarget(),
                        "status", i.getStatus(),
                        "overallScore", i.getOverallScore(),
                        "createdAt", i.getCreatedAt().toString()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDetail(Long userId, Long interviewId) {
        Interview interview = getOwnedInterview(userId, interviewId);
        List<InterviewQuestion> questions = questionRepository.findByInterviewIdOrderBySortOrderAsc(interviewId);
        List<Map<String, Object>> qList = questions.stream().map(q -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", q.getId());
            m.put("type", q.getQuestionType());
            m.put("text", q.getQuestionText());
            return m;
        }).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", interview.getId());
        result.put("title", interview.getTitle());
        result.put("status", interview.getStatus());
        result.put("overallScore", interview.getOverallScore());
        result.put("feedback", interview.getFeedback());
        result.put("improvements", interview.getImprovements());
        result.put("questions", qList);
        return result;
    }

    private Interview getOwnedInterview(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
        if (!interview.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Interview not found");
        }
        return interview;
    }

    private List<String> jsonArrayToList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) node.forEach(n -> list.add(n.asText()));
        return list;
    }
}
