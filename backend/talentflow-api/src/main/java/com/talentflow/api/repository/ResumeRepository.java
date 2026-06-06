package com.talentflow.api.repository;

import com.talentflow.api.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Page<Resume> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserId(Long userId);
}
