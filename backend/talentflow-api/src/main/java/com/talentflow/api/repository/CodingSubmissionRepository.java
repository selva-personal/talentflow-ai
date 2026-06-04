package com.talentflow.api.repository;

import com.talentflow.api.entity.CodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, Long> {
    List<CodingSubmission> findByTestIdOrderByCreatedAtDesc(Long testId);
}
