package com.talentflow.api.repository;

import com.talentflow.api.entity.CoverLetter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {
    Page<CoverLetter> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
