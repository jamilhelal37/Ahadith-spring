package com.jamil.ahadith.features.notification.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.notification.entity.Notification;

import com.jamil.ahadith.features.notification.dto.request.NotificationRequestDto;
import com.jamil.ahadith.features.notification.dto.response.NotificationResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.notification.exception.NotificationNotFoundException;
import com.jamil.ahadith.features.notification.mapper.NotificationMapper;
import com.jamil.ahadith.features.notification.repository.NotificationRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EntityManager entityManager;
    private final AdminPageService adminPageService;

    public SearchResponse<NotificationResponseDto> getNotifications(Pageable pageable) {
        return adminPageService.response(notificationRepository.findAll(pageable).map(notificationMapper::toResponseDto));
    }

    public NotificationResponseDto getNotificationById(UUID id) {
        return notificationRepository.findById(id)
                .map(notificationMapper::toResponseDto)
                .orElseThrow(NotificationNotFoundException::new);
    }

    public NotificationResponseDto createNotification(NotificationRequestDto request) {
        var notification = notificationRepository.saveAndFlush(notificationMapper.toEntity(request));
        entityManager.refresh(notification);
        return notificationMapper.toResponseDto(notification);
    }

    public void deleteNotification(UUID id) {
        if (!notificationRepository.existsById(id)) {
            throw new NotificationNotFoundException();
        }
        notificationRepository.deleteById(id);
    }
}
