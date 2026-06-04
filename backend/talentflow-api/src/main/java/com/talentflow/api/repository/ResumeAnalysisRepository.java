package com.talentflow.api.repository;

import com.talentflow.api.entity.ResumeAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    Page<ResumeAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<ResumeAnalysis> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ResumeAnalysis> findByResumeId(Long resumeId);

    @Query("SELECT AVG(r.atsScore) FROM ResumeAnalysis r WHERE r.user.id = :userId")
    Double averageAtsScoreByUserId(Long userId);

    List<ResumeAnalysis> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
