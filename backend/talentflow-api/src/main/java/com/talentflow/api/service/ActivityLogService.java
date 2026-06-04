package com.talentflow.api.service;

import com.talentflow.api.entity.ActivityLog;
import com.talentflow.api.entity.User;
import com.talentflow.api.repository.ActivityLogRepository;
import com.talentflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(Long userId, String action, String entityType, Long entityId, Map<String, Object> metadata) {
        User user = userRepository.getReferenceById(userId);
        activityLogRepository.save(ActivityLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .metadata(metadata)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<ActivityLog> getUserActivity(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
