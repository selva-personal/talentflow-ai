package com.talentflow.api.repository;

import com.talentflow.api.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findByInterviewIdOrderBySortOrderAsc(Long interviewId);
}
