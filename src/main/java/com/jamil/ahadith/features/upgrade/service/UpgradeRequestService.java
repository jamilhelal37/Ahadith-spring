package com.jamil.ahadith.features.upgrade.service;

import com.jamil.ahadith.core.exception.ConflictException;
import com.jamil.ahadith.core.exception.ForbiddenException;
import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.core.ratelimit.RateLimitKeyResolver;
import com.jamil.ahadith.core.ratelimit.RateLimitService;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.notification.entity.Notification;
import com.jamil.ahadith.features.notification.entity.NotificationType;
import com.jamil.ahadith.features.notification.repository.NotificationRepository;
import com.jamil.ahadith.features.upgrade.config.UpgradeDocumentProperties;
import com.jamil.ahadith.features.upgrade.dto.request.UpgradeDecision;
import com.jamil.ahadith.features.upgrade.dto.request.UpgradeRequestCreateDto;
import com.jamil.ahadith.features.upgrade.dto.request.UpgradeReviewRequestDto;
import com.jamil.ahadith.features.upgrade.dto.response.AdminUpgradeRequestResponseDto;
import com.jamil.ahadith.features.upgrade.dto.response.MemberUpgradeRequestResponseDto;
import com.jamil.ahadith.features.upgrade.dto.response.SignedDocumentDownloadResponseDto;
import com.jamil.ahadith.features.upgrade.entity.UpgradeRequest;
import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentStorageException;
import com.jamil.ahadith.features.upgrade.exception.UpgradeRequestNotFoundException;
import com.jamil.ahadith.features.upgrade.mapper.UpgradeRequestMapper;
import com.jamil.ahadith.features.upgrade.repository.UpgradeRequestRepository;
import com.jamil.ahadith.features.upgrade.storage.UpgradeDocumentStorageService;
import com.jamil.ahadith.features.upgrade.storage.UpgradeDocumentUploadResult;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpgradeRequestService {
    private static final Logger log = LoggerFactory.getLogger(UpgradeRequestService.class);
    private static final List<UpgradeStatus> OPEN_STATUSES = List.of(
            UpgradeStatus.pending_documents,
            UpgradeStatus.under_review
    );

    private final UpgradeRequestRepository upgradeRequestRepository;
    private final UpgradeRequestMapper upgradeRequestMapper;
    private final CurrentUserService currentUserService;
    private final AdminPageService adminPageService;
    private final NotificationRepository notificationRepository;
    private final UpgradeDocumentStorageService documentStorageService;
    private final UpgradeDocumentProperties documentProperties;
    private final RateLimitService rateLimitService;
    private final RateLimitKeyResolver rateLimitKeyResolver;
    private final UpgradeRequestTransactionService upgradeRequestTransactionService;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional(readOnly = true)
    public SearchResponse<AdminUpgradeRequestResponseDto> getUpgradeRequests(UpgradeStatus status, Pageable pageable) {
        var page = status == null
                ? upgradeRequestRepository.findAll(pageable)
                : upgradeRequestRepository.findByStatus(status, pageable);
        return adminPageService.response(page.map(upgradeRequestMapper::toAdminResponseDto));
    }

    @Transactional(readOnly = true)
    public List<MemberUpgradeRequestResponseDto> getMyUpgradeRequests() {
        var user = currentUserService.requireCurrentUser();
        return upgradeRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(upgradeRequestMapper::toMemberResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MemberUpgradeRequestResponseDto getCurrentUpgradeRequest() {
        var user = currentUserService.requireCurrentUser();
        return upgradeRequestRepository.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(user.getId(), OPEN_STATUSES)
                .map(upgradeRequestMapper::toMemberResponseDto)
                .orElseThrow(UpgradeRequestNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public AdminUpgradeRequestResponseDto getUpgradeRequestById(UUID id) {
        return upgradeRequestRepository.findById(id)
                .map(upgradeRequestMapper::toAdminResponseDto)
                .orElseThrow(UpgradeRequestNotFoundException::new);
    }

    public MemberUpgradeRequestResponseDto createUpgradeRequest(
            UpgradeRequestCreateDto request,
            HttpServletRequest servletRequest
    ) {
        User user = currentUserService.requireCurrentUser();
        if (user.getType() != UserType.member) {
            throw new ForbiddenException("Only members can request scholar access");
        }
        rateLimitService.assertAllowed(
                "upgrade-request-create",
                "user:%s:%s".formatted(user.getId(), rateLimitKeyResolver.ipKey(servletRequest))
        );
        if (upgradeRequestRepository.existsByUserIdAndStatusIn(user.getId(), OPEN_STATUSES)) {
            throw new ConflictException("An open upgrade request already exists");
        }

        UpgradeDocumentUploadResult upload = documentStorageService.upload(request.getDocument(), user.getId());
        try {
            UpgradeRequest saved = upgradeRequestTransactionService.createUpgradeRequest(user, request.getNotes(), upload);
            try {
                auditEventPublisher.publishCreate("upgrade_requests", saved.getId(), AuditData.snapshot(saved));
            } catch (Exception e) {
                log.warn("Failed to publish audit event for upgrade request creation: {}", saved.getId(), e);
            }
            return upgradeRequestMapper.toMemberResponseDto(saved);
        } catch (DataIntegrityViolationException ex) {
            deleteUploadedDocument(upload);
            throw new ConflictException("An open upgrade request already exists");
        } catch (Exception ex) {
            // Note: If transaction rolled back, UpgradeRequestCleanupListener will handle it.
            if (!(ex instanceof RuntimeException)) {
                deleteUploadedDocument(upload);
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public SignedDocumentDownloadResponseDto createMemberDownloadUrl(UUID id) {
        User user = currentUserService.requireCurrentUser();
        UpgradeRequest request = upgradeRequestRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(UpgradeRequestNotFoundException::new);
        return createDownloadResponse(request);
    }

    @Transactional(readOnly = true)
    public SignedDocumentDownloadResponseDto createAdminDownloadUrl(UUID id) {
        UpgradeRequest request = upgradeRequestRepository.findById(id)
                .orElseThrow(UpgradeRequestNotFoundException::new);
        return createDownloadResponse(request);
    }

    @Transactional
    public AdminUpgradeRequestResponseDto review(UUID id, UpgradeReviewRequestDto request) {
        User reviewer = currentUserService.requireCurrentUser();
        UpgradeRequest upgradeRequest = upgradeRequestRepository.findWithLockingById(id)
                .orElseThrow(UpgradeRequestNotFoundException::new);
        if (!OPEN_STATUSES.contains(upgradeRequest.getStatus())) {
            throw new ConflictException("Upgrade request is not open for review");
        }
        var oldData = AuditData.snapshot(upgradeRequest);

        upgradeRequest.setReviewedBy(reviewer);
        upgradeRequest.setReviewedAt(LocalDateTime.now());
        upgradeRequest.setReviewNotes(request.getReviewNotes());

        if (request.getDecision() == UpgradeDecision.APPROVE) {
            upgradeRequest.setStatus(UpgradeStatus.approved);
            upgradeRequest.getUser().setType(UserType.scholar);
            upgradeRequest.setRejectionReason(null);
            createNotification(upgradeRequest, "Scholar request approved",
                    "Your scholar upgrade request has been approved.");
        } else {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new InvalidRequestException("rejectionReason is required when rejecting an upgrade request");
            }
            upgradeRequest.setStatus(UpgradeStatus.rejected);
            upgradeRequest.setRejectionReason(request.getRejectionReason());
            createNotification(upgradeRequest, "Scholar request rejected",
                    "Your scholar upgrade request has been rejected.");
        }

        UpgradeRequest saved = upgradeRequestRepository.saveAndFlush(upgradeRequest);
        auditEventPublisher.publishUpdateAs(reviewer, "upgrade_requests", saved.getId(), oldData,
                AuditData.snapshot(saved), "reviewed upgrade request as " + saved.getStatus().name());
        return upgradeRequestMapper.toAdminResponseDto(saved);
    }

    public void deleteUpgradeRequest(UUID id) {
        upgradeRequestTransactionService.deleteUpgradeRequest(id);
    }

    private SignedDocumentDownloadResponseDto createDownloadResponse(UpgradeRequest request) {
        if (request.getDocumentPublicId() == null || request.getDocumentPublicId().isBlank()) {
            throw new UpgradeRequestNotFoundException();
        }
        Instant expiresAt = Instant.now().plus(documentProperties.getDownloadTtl());
        String downloadUrl = documentStorageService.createDownloadUrl(request.getDocumentPublicId(), expiresAt);
        return new SignedDocumentDownloadResponseDto(downloadUrl, expiresAt);
    }

    private void deleteUploadedDocument(UpgradeDocumentUploadResult upload) {
        try {
            documentStorageService.delete(upload.publicId());
        } catch (UpgradeDocumentStorageException cleanupFailure) {
            log.warn("Failed to clean up uploaded upgrade document publicIdPresent={}",
                    upload.publicId() != null);
        }
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

}
