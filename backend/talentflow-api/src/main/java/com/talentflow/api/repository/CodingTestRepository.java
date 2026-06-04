package com.talentflow.api.repository;

import com.talentflow.api.entity.CodingTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodingTestRepository extends JpaRepository<CodingTest, Long> {
    Page<CodingTest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
