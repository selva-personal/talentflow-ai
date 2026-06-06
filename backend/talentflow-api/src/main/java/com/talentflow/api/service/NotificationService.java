package com.talentflow.api.service;

import com.talentflow.api.entity.Notification;
import com.talentflow.api.entity.User;
import com.talentflow.api.exception.ResourceNotFoundException;
import com.talentflow.api.repository.NotificationRepository;
import com.talentflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notify(Long userId, String title, String message, String type) {
        User user = userRepository.getReferenceById(userId);
        notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> list(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toMap);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    private Map<String, Object> toMap(Notification n) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", n.getId());
        map.put("title", n.getTitle());
        map.put("message", n.getMessage());
        map.put("type", n.getType());
        map.put("read", n.isRead());
        map.put("createdAt", n.getCreatedAt().toString());
        return map;
    }
}
