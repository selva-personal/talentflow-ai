package com.talentflow.api.repository;

import com.talentflow.api.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
