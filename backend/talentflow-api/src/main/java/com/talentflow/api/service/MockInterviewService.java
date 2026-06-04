package com.talentflow.api.service;

import com.talentflow.api.ai.GeminiService;
import com.talentflow.api.dto.request.CreateMockSessionRequest;
import com.talentflow.api.dto.request.MockInterviewMessageRequest;
import com.talentflow.api.entity.MockInterviewSession;
import com.talentflow.api.entity.User;
import com.talentflow.api.exception.ResourceNotFoundException;
import com.talentflow.api.repository.MockInterviewSessionRepository;
import com.talentflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockInterviewSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ActivityLogService activityLogService;

    @Transactional
    public Map<String, Object> createSession(Long userId, CreateMockSessionRequest req) {
        User user = userRepository.getReferenceById(userId);
        List<Map<String, Object>> messages = new ArrayList<>();
        String opener = geminiService.generate(
                "You are a professional AI interviewer. Start the mock interview with a brief greeting and first question.",
                "Role: " + (req.getRoleTarget() != null ? req.getRoleTarget() : "Software Engineer"));
        messages.add(Map.of("role", "assistant", "content", opener, "timestamp", java.time.Instant.now().toString()));

        MockInterviewSession session = MockInterviewSession.builder()
                .user(user)
                .title(req.getTitle())
                .roleTarget(req.getRoleTarget())
                .status("ACTIVE")
                .messages(messages)
                .progressPercent(5)
                .build();
        session = sessionRepository.save(session);
        activityLogService.log(userId, "MOCK_SESSION_CREATED", "MOCK_INTERVIEW", session.getId(), null);
        return sessionToMap(session);
    }

    @Transactional
    public Map<String, Object> sendMessage(Long userId, Long sessionId, MockInterviewMessageRequest req) {
        MockInterviewSession session = getOwned(userId, sessionId);
        List<Map<String, Object>> messages = new ArrayList<>(session.getMessages());
        messages.add(Map.of("role", "user", "content", req.getMessage(), "timestamp", java.time.Instant.now().toString()));

        StringBuilder history = new StringBuilder();
        for (Map<String, Object> m : messages) {
            history.append(m.get("role")).append(": ").append(m.get("content")).append("\n");
        }
        String reply = geminiService.generate(
                "You are an AI interviewer conducting a mock interview. Ask follow-up questions, evaluate briefly, stay professional.",
                history.toString());
        messages.add(Map.of("role", "assistant", "content", reply, "timestamp", java.time.Instant.now().toString()));

        int progress = Math.min(100, session.getProgressPercent() + 8);
        session.setMessages(messages);
        session.setProgressPercent(progress);
        sessionRepository.save(session);

        return Map.of(
                "sessionId", session.getId(),
                "reply", reply,
                "progressPercent", progress,
                "messages", messages);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSession(Long userId, Long sessionId) {
        return sessionToMap(getOwned(userId, sessionId));
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listSessions(Long userId, Pageable pageable) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)
                .map(this::sessionToMap);
    }

    private MockInterviewSession getOwned(Long userId, Long sessionId) {
        MockInterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!session.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Session not found");
        }
        return session;
    }

    private Map<String, Object> sessionToMap(MockInterviewSession s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("title", s.getTitle());
        m.put("roleTarget", s.getRoleTarget());
        m.put("status", s.getStatus());
        m.put("progressPercent", s.getProgressPercent());
        m.put("messages", s.getMessages());
        m.put("updatedAt", s.getUpdatedAt().toString());
        return m;
    }

}
