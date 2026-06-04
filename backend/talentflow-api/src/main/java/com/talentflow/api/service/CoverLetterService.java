package com.talentflow.api.service;

import com.talentflow.api.ai.GeminiService;
import com.talentflow.api.dto.request.CoverLetterRequest;
import com.talentflow.api.entity.CoverLetter;
import com.talentflow.api.entity.User;
import com.talentflow.api.exception.ResourceNotFoundException;
import com.talentflow.api.repository.CoverLetterRepository;
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
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ActivityLogService activityLogService;

    @Transactional
    public Map<String, Object> generate(Long userId, CoverLetterRequest req) {
        String system = "Write a professional cover letter. Return plain text only, no markdown fences.";
        String prompt = String.format("Job: %s at %s. Tone: %s. Highlights: %s",
                req.getJobTitle(), req.getCompanyName(), req.getTone(),
                req.getHighlights() != null ? req.getHighlights() : "experienced professional");
        String content = geminiService.generate(system, prompt);

        User user = userRepository.getReferenceById(userId);
        CoverLetter letter = CoverLetter.builder()
                .user(user)
                .jobTitle(req.getJobTitle())
                .companyName(req.getCompanyName())
                .content(content.trim())
                .tone(req.getTone())
                .build();
        letter = coverLetterRepository.save(letter);
        activityLogService.log(userId, "COVER_LETTER_GENERATED", "COVER_LETTER", letter.getId(), null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", letter.getId());
        result.put("jobTitle", letter.getJobTitle());
        result.put("companyName", letter.getCompanyName());
        result.put("content", letter.getContent());
        result.put("tone", letter.getTone());
        return result;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> list(Long userId, Pageable pageable) {
        return coverLetterRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(l -> Map.<String, Object>of(
                        "id", l.getId(),
                        "jobTitle", l.getJobTitle(),
                        "companyName", l.getCompanyName(),
                        "createdAt", l.getCreatedAt().toString()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(Long userId, Long id) {
        CoverLetter letter = coverLetterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cover letter not found"));
        if (!letter.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Cover letter not found");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", letter.getId());
        result.put("jobTitle", letter.getJobTitle());
        result.put("companyName", letter.getCompanyName());
        result.put("content", letter.getContent());
        result.put("tone", letter.getTone());
        return result;
    }
}
