package com.talentflow.api.repository;

import com.talentflow.api.entity.MockInterviewSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MockInterviewSessionRepository extends JpaRepository<MockInterviewSession, Long> {
    Page<MockInterviewSession> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
}
