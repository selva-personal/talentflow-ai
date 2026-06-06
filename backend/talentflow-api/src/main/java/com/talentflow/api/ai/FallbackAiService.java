package com.talentflow.api.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.talentflow.api.dto.request.CareerRoadmapRequest;
import com.talentflow.api.dto.request.CodingSubmitRequest;
import com.talentflow.api.dto.request.CodingTestRequest;
import com.talentflow.api.dto.request.CoverLetterRequest;
import com.talentflow.api.dto.request.GenerateInterviewRequest;
import com.talentflow.api.entity.Interview;
import com.talentflow.api.entity.InterviewQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FallbackAiService {

    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE = Pattern.compile("(\\+?\\d[\\d\\s()-]{7,}\\d)");
    private static final List<String> SKILL_KEYWORDS = List.of(
            "java", "python", "javascript", "typescript", "react", "spring", "sql", "aws",
            "docker", "kubernetes", "node", "angular", "vue", "git", "api", "rest", "graphql",
            "mongodb", "postgresql", "redis", "kafka", "microservices", "agile", "scrum");

    private final ObjectMapper objectMapper;

    public JsonNode analyzeResume(String resumeText) {
        String text = resumeText != null ? resumeText.toLowerCase() : "";
        int score = 45;
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (text.length() > 500) {
            score += 10;
            strengths.add("Resume has sufficient detail and length");
        } else {
            weaknesses.add("Resume content is too short for ATS optimization");
            recommendations.add("Expand bullet points with measurable achievements");
        }
        if (EMAIL.matcher(text).find()) {
            score += 5;
            strengths.add("Contact email detected");
        } else {
            weaknesses.add("No email address detected");
        }
        if (PHONE.matcher(text).find()) {
            score += 5;
            strengths.add("Phone number detected");
        }
        if (text.contains("education") || text.contains("university") || text.contains("bachelor")
                || text.contains("degree")) {
            score += 10;
            strengths.add("Education section detected");
        } else {
            weaknesses.add("Education section not clearly identified");
            recommendations.add("Add a dedicated Education section with degree and institution");
        }
        if (text.contains("experience") || text.contains("work history") || text.contains("employment")) {
            score += 10;
            strengths.add("Work experience section detected");
        } else {
            weaknesses.add("Work experience section not clearly identified");
        }

        List<String> foundSkills = new ArrayList<>();
        for (String skill : SKILL_KEYWORDS) {
            if (text.contains(skill)) {
                foundSkills.add(skill);
            } else if (missingSkills.size() < 6) {
                missingSkills.add(skill);
            }
        }
        if (!foundSkills.isEmpty()) {
            score += Math.min(20, foundSkills.size() * 3);
            strengths.add("Technical skills detected: " + String.join(", ", foundSkills.stream().limit(5).toList()));
        }
        if (text.contains("project")) {
            score += 5;
            strengths.add("Projects section or project mentions found");
        } else {
            recommendations.add("Add a Projects section highlighting impact and technologies used");
        }
        if (text.contains("certification") || text.contains("certified")) {
            score += 5;
            strengths.add("Certifications mentioned");
        }

        score = Math.min(92, Math.max(35, score));
        if (recommendations.isEmpty()) {
            recommendations.add("Tailor keywords to match target job descriptions");
            recommendations.add("Use action verbs and quantify results in each bullet");
            recommendations.add("Keep formatting ATS-friendly (standard headings, no tables)");
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("atsScore", score);
        root.set("strengths", toArray(strengths));
        root.set("weaknesses", toArray(weaknesses));
        root.set("missingSkills", toArray(missingSkills.stream().limit(6).toList()));
        root.set("recommendations", toArray(recommendations));
        return root;
    }

    public JsonNode generateInterviewQuestions(GenerateInterviewRequest req) {
        String role = req.getRoleTarget() != null ? req.getRoleTarget().toLowerCase() : "software engineer";
        String type = req.getInterviewType() != null ? req.getInterviewType().toUpperCase() : "MIXED";

        List<Map<String, String>> questions = new ArrayList<>();
        if (role.contains("front") || role.contains("react") || role.contains("ui")) {
            addQuestions(questions, "TECHNICAL", List.of(
                    "What is React and why is it used for building UIs?",
                    "Explain the Virtual DOM and how React uses it.",
                    "What is useEffect and when would you use it?",
                    "How do you manage state in a React application?",
                    "Explain the difference between controlled and uncontrolled components."));
        } else if (role.contains("back") || role.contains("spring") || role.contains("java")) {
            addQuestions(questions, "TECHNICAL", List.of(
                    "What is Spring Boot and what problems does it solve?",
                    "Explain Dependency Injection in Spring.",
                    "How does REST API design differ from GraphQL?",
                    "Describe how you would secure a Spring Boot application.",
                    "What is the difference between JPA and JDBC?"));
        } else {
            addQuestions(questions, "TECHNICAL", List.of(
                    "Explain the difference between SQL and NoSQL databases.",
                    "What is object-oriented programming?",
                    "Describe how HTTP request/response cycle works.",
                    "What is version control and how do you use Git in a team?",
                    "Explain time and space complexity with an example."));
        }

        if ("SYSTEM_DESIGN".equals(type) || "MIXED".equals(type)) {
            addQuestions(questions, "SYSTEM_DESIGN", List.of(
                    "Explain microservices architecture and its trade-offs.",
                    "How would you design a URL shortener at scale?",
                    "Describe caching strategies for a high-traffic API."));
        }
        if ("BEHAVIORAL".equals(type) || "MIXED".equals(type)) {
            addQuestions(questions, "BEHAVIORAL", List.of(
                    "Tell me about a challenging project and how you handled it.",
                    "Describe a time you had a conflict with a teammate.",
                    "Give an example of when you had to learn a technology quickly."));
        }
        if ("HR".equals(type) || "MIXED".equals(type)) {
            addQuestions(questions, "HR", List.of(
                    "Why are you interested in this role?",
                    "Where do you see yourself in three years?",
                    "What are your salary expectations for this position?"));
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("title", req.getRoleTarget() + " Interview (Offline Mode)");
        ArrayNode qArray = objectMapper.createArrayNode();
        for (Map<String, String> q : questions.stream().limit(10).toList()) {
            ObjectNode qNode = objectMapper.createObjectNode();
            qNode.put("type", q.get("type"));
            qNode.put("text", q.get("text"));
            qArray.add(qNode);
        }
        root.set("questions", qArray);
        return root;
    }

    public JsonNode scoreAnswer(String questionType, String questionText, String answerText) {
        int score = 55;
        String feedback;
        int len = answerText != null ? answerText.trim().length() : 0;
        if (len > 200) score += 15;
        if (len > 80) score += 10;
        if (len < 30) score -= 15;
        if (answerText != null && questionText != null) {
            String[] words = questionText.toLowerCase().split("\\W+");
            int matches = 0;
            String lowerAnswer = answerText.toLowerCase();
            for (String w : words) {
                if (w.length() > 4 && lowerAnswer.contains(w)) matches++;
            }
            score += Math.min(15, matches * 3);
        }
        score = Math.min(88, Math.max(40, score));
        feedback = score >= 75
                ? "Solid offline evaluation: answer covers key concepts with reasonable depth."
                : "Offline evaluation: expand your answer with examples, structure, and role-specific keywords.";

        ObjectNode root = objectMapper.createObjectNode();
        root.put("score", score);
        root.put("feedback", feedback + " (Generated using offline rules — Gemini unavailable.)");
        return root;
    }

    public JsonNode completeInterview(Interview interview, int averageScore) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("feedback", String.format(
                "Offline summary for %s: average score %d/100. Focus on structured answers and concrete examples.",
                interview.getRoleTarget(), averageScore));
        ArrayNode improvements = objectMapper.createArrayNode();
        improvements.add("Practice STAR method for behavioral questions");
        improvements.add("Review core concepts for " + interview.getRoleTarget());
        improvements.add("Retry with Gemini when quota is restored for deeper AI feedback");
        root.set("improvements", improvements);
        return root;
    }

    public JsonNode generateCodingChallenge(CodingTestRequest req) {
        String lang = req.getLanguage() != null ? req.getLanguage() : "JavaScript";
        ObjectNode root = objectMapper.createObjectNode();
        root.put("title", "Two Sum (Offline Challenge)");
        root.put("problemStatement",
                "Given an array of integers and a target, return indices of the two numbers that add up to the target.\n"
                        + "Assume exactly one solution exists.");
        root.put("starterCode", switch (lang.toLowerCase()) {
            case "python" -> "def two_sum(nums, target):\n    # return [i, j]\n    pass\n";
            case "java" -> "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        return new int[0];\n    }\n}\n";
            default -> "function twoSum(nums, target) {\n  // return [i, j]\n}\n";
        });
        return root;
    }

    public JsonNode reviewCode(CodingTestRequest req, String problem, String code) {
        int score = code != null && code.length() > 40 ? 72 : 50;
        boolean passed = code != null && code.length() > 20;
        ObjectNode root = objectMapper.createObjectNode();
        root.put("passed", passed);
        root.put("output", passed ? "[0, 1] (simulated offline result)" : "Incomplete submission");
        root.put("aiScore", score);
        root.put("complexityAnalysis", "Offline estimate: O(n) time, O(n) space for hash-map approach.");
        ArrayNode suggestions = objectMapper.createArrayNode();
        suggestions.add("Use a hash map to store complements for O(n) time");
        suggestions.add("Handle edge cases: empty array, no solution");
        suggestions.add("Add test cases before submitting");
        root.set("suggestions", suggestions);
        return root;
    }

    public JsonNode reviewCodeSubmission(String language, String problem, String code) {
        CodingTestRequest req = new CodingTestRequest();
        req.setLanguage(language);
        return reviewCode(req, problem, code);
    }

    public JsonNode generateRoadmap(CareerRoadmapRequest req) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode skills = objectMapper.createArrayNode();
        addSkill(skills, "Core programming fundamentals", "high");
        addSkill(skills, req.getTargetRole() + " stack", "high");
        addSkill(skills, "System design basics", "medium");
        addSkill(skills, "Communication & leadership", "medium");
        root.set("skills", skills);

        ArrayNode path = objectMapper.createArrayNode();
        addPhase(path, "Foundation", List.of("Data structures", "Algorithms", "Git workflow"), 8);
        addPhase(path, "Role specialization", List.of("Framework deep dive", "Production projects"), 12);
        addPhase(path, "Interview readiness", List.of("Mock interviews", "Portfolio polish"), 6);
        root.set("learningPath", path);

        ArrayNode projects = objectMapper.createArrayNode();
        addProject(projects, "Portfolio API", "Build a REST API with auth, tests, and deployment.");
        addProject(projects, "Capstone app", "End-to-end app demonstrating " + req.getTargetRole() + " skills.");
        root.set("projects", projects);

        ArrayNode milestones = objectMapper.createArrayNode();
        int months = Math.max(3, req.getTimelineMonths());
        milestones.add(milestone(1, "Complete foundation modules"));
        milestones.add(milestone(months / 2, "Ship first portfolio project"));
        milestones.add(milestone(months, "Reach target role readiness: " + req.getTargetRole()));
        root.set("milestones", milestones);
        return root;
    }

    public String generateCoverLetter(CoverLetterRequest req) {
        return String.format("""
                Dear Hiring Manager,

                I am writing to express my interest in the %s position at %s. With a %s approach and a track record
                of delivering reliable software solutions, I am confident I would contribute meaningfully to your team.

                %s

                I am eager to bring my technical skills, collaboration mindset, and continuous learning habits to %s.
                Thank you for considering my application. I look forward to discussing how I can support your goals.

                Sincerely,
                [Your Name]

                ---
                [Generated in offline mode — Gemini AI temporarily unavailable]
                """,
                req.getJobTitle(),
                req.getCompanyName(),
                req.getTone() != null ? req.getTone().toLowerCase() : "professional",
                req.getHighlights() != null ? req.getHighlights() : "I have experience building scalable applications and working cross-functionally.",
                req.getCompanyName());
    }

    public String mockInterviewOpener(String roleTarget) {
        return "Hello! I'll be conducting your mock interview today for the "
                + (roleTarget != null ? roleTarget : "Software Engineer")
                + " role. Let's start: tell me about yourself and what interests you about this position."
                + "\n\n[Offline interviewer mode — responses use template follow-ups.]";
    }

    public String mockInterviewReply(String roleTarget, String conversationHistory) {
        List<String> followUps = List.of(
                "Can you walk me through a recent project and your specific contributions?",
                "How do you handle tight deadlines and competing priorities?",
                "Describe a technical decision you made that had significant impact.",
                "What would you improve in your last project's architecture?",
                "Do you have any questions for me about the team or role?");
        int index = Math.abs(conversationHistory.hashCode()) % followUps.size();
        return followUps.get(index) + "\n\n[Offline mode — Gemini unavailable. Answer naturally to practice.]";
    }

    public String careerRecommendations(Double avgAts, Double avgInterview) {
        return "[\"Upload an updated resume to improve ATS score\","
                + "\"Complete weekly mock interviews\","
                + "\"Build one portfolio project aligned with your target role\"]";
    }

    private void addQuestions(List<Map<String, String>> list, String type, List<String> texts) {
        for (String text : texts) {
            list.add(Map.of("type", type, "text", text));
        }
    }

    private ArrayNode toArray(List<String> items) {
        ArrayNode arr = objectMapper.createArrayNode();
        items.forEach(arr::add);
        return arr;
    }

    private void addSkill(ArrayNode arr, String name, String priority) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("priority", priority);
        arr.add(node);
    }

    private void addPhase(ArrayNode arr, String phase, List<String> topics, int weeks) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("phase", phase);
        node.put("durationWeeks", weeks);
        node.set("topics", toArray(topics));
        arr.add(node);
    }

    private void addProject(ArrayNode arr, String title, String description) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", title);
        node.put("description", description);
        arr.add(node);
    }

    private ObjectNode milestone(int month, String goal) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("month", month);
        node.put("goal", goal);
        return node;
    }
}
