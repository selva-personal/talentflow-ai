package com.talentflow.api.repository;

import com.talentflow.api.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Page<Interview> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT AVG(i.overallScore) FROM Interview i WHERE i.user.id = :userId AND i.overallScore IS NOT NULL")
    Double averageScoreByUserId(Long userId);

    long countByUserId(Long userId);
}
