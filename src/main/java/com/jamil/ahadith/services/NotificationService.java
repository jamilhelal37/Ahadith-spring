package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.NotificationRequestDto;
import com.jamil.ahadith.dtos.responses.NotificationResponseDto;
import com.jamil.ahadith.exceptions.NotificationNotFoundException;
import com.jamil.ahadith.mappers.NotificationMapper;
import com.jamil.ahadith.repositories.NotificationRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EntityManager entityManager;

    public List<NotificationResponseDto> getNotifications() {
        return notificationRepository.findAll().stream()
                .map(notificationMapper::toResponseDto)
                .toList();
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
