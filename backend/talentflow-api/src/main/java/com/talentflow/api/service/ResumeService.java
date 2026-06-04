package com.talentflow.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.talentflow.api.ai.GeminiService;
import com.talentflow.api.config.TalentflowProperties;
import com.talentflow.api.entity.Resume;
import com.talentflow.api.entity.ResumeAnalysis;
import com.talentflow.api.entity.User;
import com.talentflow.api.exception.BadRequestException;
import com.talentflow.api.exception.ResourceNotFoundException;
import com.talentflow.api.repository.ResumeAnalysisRepository;
import com.talentflow.api.repository.ResumeRepository;
import com.talentflow.api.repository.UserRepository;
import com.talentflow.api.util.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final GeminiService geminiService;
    private final TalentflowProperties properties;
    private final ActivityLogService activityLogService;

    @Transactional
    public Map<String, Object> uploadAndAnalyze(Long userId, MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new BadRequestException("File is empty");
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new BadRequestException("Only PDF files are supported");
        }
        Path uploadDir = Paths.get(properties.getUpload().getDirectory(), String.valueOf(userId));
        Files.createDirectories(uploadDir);
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = uploadDir.resolve(storedName);
        Files.write(target, file.getBytes());

        String text = pdfTextExtractor.extract(target);
        User user = userRepository.getReferenceById(userId);
        Resume resume = Resume.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .filePath(target.toString())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .extractedText(text)
                .build();
        resume = resumeRepository.save(resume);

        ResumeAnalysis analysis = analyzeResume(userId, resume, text);
        activityLogService.log(userId, "RESUME_ANALYZED", "RESUME", resume.getId(), Map.of("atsScore", analysis.getAtsScore()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resumeId", resume.getId());
        result.put("fileName", resume.getFileName());
        result.put("analysis", toAnalysisMap(analysis));
        return result;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listResumes(Long userId, Pageable pageable) {
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "fileName", r.getFileName(),
                        "fileSize", r.getFileSize(),
                        "createdAt", r.getCreatedAt().toString()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnalysis(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        if (!resume.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Resume not found");
        }
        ResumeAnalysis analysis = analysisRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
        return toAnalysisMap(analysis);
    }

    private ResumeAnalysis analyzeResume(Long userId, Resume resume, String text) {
        String system = """
                You are an expert ATS resume analyzer. Respond ONLY with valid JSON:
                {
                  "atsScore": <0-100 integer>,
                  "strengths": ["..."],
                  "weaknesses": ["..."],
                  "missingSkills": ["..."],
                  "recommendations": ["..."]
                }
                """;
        String userPrompt = "Analyze this resume:\n\n" + text.substring(0, Math.min(text.length(), 12000));
        String aiText = geminiService.generate(system, userPrompt);
        JsonNode json = geminiService.parseJsonResponse(aiText);

        User user = userRepository.getReferenceById(userId);
        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .resume(resume)
                .user(user)
                .atsScore(json.path("atsScore").asInt(75))
                .strengths(jsonArrayToList(json.path("strengths")))
                .weaknesses(jsonArrayToList(json.path("weaknesses")))
                .missingSkills(jsonArrayToList(json.path("missingSkills")))
                .recommendations(jsonArrayToList(json.path("recommendations")))
                .rawAiResponse(Map.of("raw", aiText))
                .build();
        return analysisRepository.save(analysis);
    }

    private List<String> jsonArrayToList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(n -> list.add(n.asText()));
        }
        return list;
    }

    private Map<String, Object> toAnalysisMap(ResumeAnalysis a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("resumeId", a.getResume().getId());
        m.put("atsScore", a.getAtsScore());
        m.put("strengths", a.getStrengths());
        m.put("weaknesses", a.getWeaknesses());
        m.put("missingSkills", a.getMissingSkills());
        m.put("recommendations", a.getRecommendations());
        m.put("createdAt", a.getCreatedAt().toString());
        return m;
    }
}
