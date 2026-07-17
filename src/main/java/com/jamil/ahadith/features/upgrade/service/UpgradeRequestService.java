package com.jamil.ahadith.features.upgrade.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.upgrade.dto.request.UpgradeDecision;

import com.jamil.ahadith.features.user.service.CurrentUserService;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.features.upgrade.dto.request.UpgradeRequestDto;
import com.jamil.ahadith.features.upgrade.dto.request.UpgradeReviewRequestDto;
import com.jamil.ahadith.features.upgrade.dto.response.UpgradeRequestResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.audit.entity.ActivityLog;
import com.jamil.ahadith.features.notification.entity.Notification;
import com.jamil.ahadith.features.notification.entity.NotificationType;
import com.jamil.ahadith.features.upgrade.entity.UpgradeRequest;
import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.core.exception.ConflictException;
import com.jamil.ahadith.core.exception.ForbiddenException;
import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.upgrade.exception.UpgradeRequestNotFoundException;
import com.jamil.ahadith.features.upgrade.mapper.UpgradeRequestMapper;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.notification.repository.NotificationRepository;
import com.jamil.ahadith.features.upgrade.repository.UpgradeRequestRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class UpgradeRequestService {
    private final UpgradeRequestRepository upgradeRequestRepository;
    private final UpgradeRequestMapper upgradeRequestMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final AdminPageService adminPageService;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationRepository notificationRepository;
    private final Environment environment;

    private static final List<UpgradeStatus> OPEN_STATUSES = List.of(UpgradeStatus.pending_documents, UpgradeStatus.under_review);

    public SearchResponse<UpgradeRequestResponseDto> getUpgradeRequests(UpgradeStatus status, Pageable pageable) {
        var page = status == null
                ? upgradeRequestRepository.findAll(pageable)
                : upgradeRequestRepository.findByStatus(status, pageable);
        return adminPageService.response(page.map(upgradeRequestMapper::toResponseDto));
    }

    public List<UpgradeRequestResponseDto> getMyUpgradeRequests() {
        var user = currentUserService.requireCurrentUser();
        return upgradeRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(upgradeRequestMapper::toResponseDto)
                .toList();
    }

    public UpgradeRequestResponseDto getCurrentUpgradeRequest() {
        var user = currentUserService.requireCurrentUser();
        return upgradeRequestRepository.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(user.getId(), OPEN_STATUSES)
                .map(upgradeRequestMapper::toResponseDto)
                .orElseThrow(UpgradeRequestNotFoundException::new);
    }

    public UpgradeRequestResponseDto getUpgradeRequestById(UUID id) {
        return upgradeRequestRepository.findById(id)
                .map(upgradeRequestMapper::toResponseDto)
                .orElseThrow(UpgradeRequestNotFoundException::new);
    }

    public UpgradeRequestResponseDto createUpgradeRequest(UpgradeRequestDto request) {
        var user = currentUserService.requireCurrentUser();
        if (user.getType() != UserType.member) {
            throw new ForbiddenException("Only members can request scholar access");
        }
        if (upgradeRequestRepository.existsByUserIdAndStatusIn(user.getId(), OPEN_STATUSES)) {
            throw new ConflictException("An open upgrade request already exists");
        }
        UpgradeRequest upgradeRequest = upgradeRequestMapper.toEntity(request);
        upgradeRequest.setUser(user);
        upgradeRequest.setStatus(UpgradeStatus.pending_documents);
        var saved = upgradeRequestRepository.saveAndFlush(upgradeRequest);
        entityManager.refresh(saved);
        return upgradeRequestMapper.toResponseDto(saved);
    }

    public UpgradeRequestResponseDto updateUpgradeRequest(UUID id, UpgradeRequestDto request) {
        var upgradeRequest = upgradeRequestRepository.findById(id)
                .orElseThrow(UpgradeRequestNotFoundException::new);
        upgradeRequestMapper.updateEntity(request, upgradeRequest);
        var saved = upgradeRequestRepository.saveAndFlush(upgradeRequest);
        entityManager.refresh(saved);
        return upgradeRequestMapper.toResponseDto(saved);
    }

    public UpgradeRequestResponseDto review(UUID id, UpgradeReviewRequestDto request) {
        var reviewer = currentUserService.requireCurrentUser();
        var upgradeRequest = upgradeRequestRepository.findById(id)
                .orElseThrow(UpgradeRequestNotFoundException::new);
        lockForReview(upgradeRequest);
        if (!OPEN_STATUSES.contains(upgradeRequest.getStatus())) {
            throw new InvalidRequestException("Upgrade request is not open for review");
        }

        upgradeRequest.setReviewedBy(reviewer);
        upgradeRequest.setReviewedAt(LocalDateTime.now());
        upgradeRequest.setReviewNotes(request.getReviewNotes());

        if (request.getDecision() == com.jamil.ahadith.features.upgrade.dto.request.UpgradeDecision.APPROVE) {
            upgradeRequest.setStatus(UpgradeStatus.approved);
            upgradeRequest.getUser().setType(UserType.scholar);
            createNotification(upgradeRequest, "Scholar request approved",
                    "Your scholar upgrade request has been approved.");
        } else {
            upgradeRequest.setStatus(UpgradeStatus.rejected);
            upgradeRequest.setRejectionReason(request.getRejectionReason());
            createNotification(upgradeRequest, "Scholar request rejected",
                    "Your scholar upgrade request has been rejected.");
        }

        createAuditLog(reviewer, upgradeRequest);
        var saved = upgradeRequestRepository.saveAndFlush(upgradeRequest);
        entityManager.refresh(saved);
        return upgradeRequestMapper.toResponseDto(saved);
    }

    private void lockForReview(UpgradeRequest upgradeRequest) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return;
        }
        entityManager.lock(upgradeRequest, LockModeType.PESSIMISTIC_WRITE);
    }

    private void createNotification(UpgradeRequest request, String title, String body) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setBody(body);
        notification.setType(NotificationType.general);
        notification.setUser(request.getUser());
        notification.setCreatedBy(request.getReviewedBy());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(notification.getCreatedAt());
        notificationRepository.save(notification);
    }

    private void createAuditLog(com.jamil.ahadith.features.user.entity.User reviewer, UpgradeRequest request) {
        ActivityLog log = new ActivityLog();
        log.setActorUserId(reviewer.getId());
        log.setActorName(reviewer.getName());
        log.setActorEmail(reviewer.getEmail());
        log.setActorAvatarUrl(reviewer.getAvatarUrl());
        log.setMessage("reviewed upgrade request as " + request.getStatus().name());
        log.setTableName("upgrade_requests");
        log.setRecordId(request.getId());
        log.setNewData(Map.of("status", request.getStatus().name(), "userId", request.getUser().getId().toString()));
        activityLogRepository.save(log);
    }

    public void deleteUpgradeRequest(UUID id) {
        if (!upgradeRequestRepository.existsById(id)) {
            throw new UpgradeRequestNotFoundException();
        }
        upgradeRequestRepository.deleteById(id);
    }
}
