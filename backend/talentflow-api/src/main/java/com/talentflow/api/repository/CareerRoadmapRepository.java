package com.talentflow.api.repository;

import com.talentflow.api.entity.CareerRoadmap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRoadmapRepository extends JpaRepository<CareerRoadmap, Long> {
    Page<CareerRoadmap> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
