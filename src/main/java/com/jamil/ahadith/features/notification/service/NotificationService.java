package com.jamil.ahadith.features.notification.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.hadith.dto.request.reference.FakeHadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.exception.FakeHadithNotFoundException;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.HadithReferenceResponseMapper;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.notification.dto.request.NotificationRequestDto;
import com.jamil.ahadith.features.notification.dto.response.NotificationResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.notification.entity.Notification;
import com.jamil.ahadith.features.notification.exception.NotificationNotFoundException;
import com.jamil.ahadith.features.notification.mapper.NotificationMapper;
import com.jamil.ahadith.features.notification.repository.NotificationRepository;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional
@AllArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EntityManager entityManager;
    private final AdminPageService adminPageService;
    private final HadithRepository hadithRepository;
    private final FakeHadithRepository fakeHadithRepository;
    private final CurrentUserService currentUserService;
    private final AuditEventPublisher auditEventPublisher;
    private final HadithSearchService hadithSearchService;

    public SearchResponse<NotificationResponseDto> getNotifications(Pageable pageable) {
        Page<Notification> page = notificationRepository.findAll(pageable);
        Map<UUID, HadithReferenceResponseDto> hadithMap =
                loadHadithReferences(page.getContent());

        return adminPageService.response(
                page.map(notification ->
                        toResponseWithFullHadith(
                                notification,
                                hadithMap
                        )
                )
        );
    }

    public NotificationResponseDto getNotificationById(UUID id) {
        return notificationRepository.findById(id)
                .map(this::toResponseWithFullHadith)
                .orElseThrow(NotificationNotFoundException::new);
    }

    public NotificationResponseDto createNotification(NotificationRequestDto request) {
        var entity = notificationMapper.toEntity(request);
        entity.setHadith(resolveHadith(request.getHadith()));
        entity.setFakeHadith(resolveFakeHadith(request.getFakeHadith()));
        currentUserService.getCurrentUser().ifPresent(entity::setCreatedBy);
        var notification = notificationRepository.saveAndFlush(entity);
        entityManager.refresh(notification);
        auditEventPublisher.publishCreate("notifications", notification.getId(), AuditData.snapshot(notification));
        return toResponseWithFullHadith(notification);
    }

    private NotificationResponseDto toResponseWithFullHadith(
            Notification notification) {

        if (notification.getHadith() == null) {
            var dto = notificationMapper.toResponseDto(notification);
            dto.setHadith(null);
            return dto;
        }

        return toResponseWithFullHadith(
                notification,
                loadHadithReferences(List.of(notification))
        );
    }

    private NotificationResponseDto toResponseWithFullHadith(
            Notification notification,
            Map<UUID, HadithReferenceResponseDto> hadithMap) {

        var dto = notificationMapper.toResponseDto(notification);

        if (notification.getHadith() == null) {
            dto.setHadith(null);
        } else {
            dto.setHadith(
                    hadithMap.get(notification.getHadith().getId())
            );
        }

        return dto;
    }

    private Map<UUID, HadithReferenceResponseDto> loadHadithReferences(
            List<Notification> notifications) {

        List<UUID> ids = notifications.stream()
                .map(Notification::getHadith)
                .filter(Objects::nonNull)
                .map(Hadith::getId)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            return Map.of();
        }

        return hadithSearchService
                .getHadithCardsByIdsInOrder(ids)
                .stream()
                .map(HadithReferenceResponseMapper::fromSearchItem)
                .collect(Collectors.toMap(
                        HadithReferenceResponseDto::getId,
                        Function.identity()
                ));
    }

    public void deleteNotification(UUID id) {
        var notification = notificationRepository.findById(id).orElseThrow(NotificationNotFoundException::new);
        var oldData = AuditData.snapshot(notification);
        notificationRepository.delete(notification);
        auditEventPublisher.publishDelete("notifications", id, oldData);
    }

    private Hadith resolveHadith(HadithReferenceRequestDto reference) {
        if (reference == null) {
            return null;
        }
        return hadithRepository.findById(reference.getId())
                .orElseThrow(HadithNotFoundException::new);
    }

    private FakeHadith resolveFakeHadith(FakeHadithReferenceRequestDto reference) {
        if (reference == null) {
            return null;
        }
        return fakeHadithRepository.findById(reference.getId())
                .orElseThrow(FakeHadithNotFoundException::new);
    }
}
