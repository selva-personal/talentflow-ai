package com.talentflow.api.repository;

import com.talentflow.api.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {
    java.util.Optional<InterviewAnswer> findTopByQuestionIdOrderByCreatedAtDesc(Long questionId);
}
