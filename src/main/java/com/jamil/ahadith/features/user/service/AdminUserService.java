package com.jamil.ahadith.features.user.service;

import com.jamil.ahadith.core.exception.ForbiddenException;
import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.core.security.RefreshTokenRevoker;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.user.dto.request.AdminUserStatusUpdateRequestDto;
import com.jamil.ahadith.features.user.dto.request.AdminUserTypeUpdateRequestDto;
import com.jamil.ahadith.features.user.dto.response.AdminUserResponseDto;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.exception.UserNotFoundException;
import com.jamil.ahadith.features.user.mapper.AdminUserMapper;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private static final String USERS_TABLE_NAME = "users";
    private static final Set<String> ALLOWED_SORTS = Set.of(
            "createdAt", "updatedAt", "id", "name", "email", "status", "type"
    );
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt")
            .and(Sort.by(Sort.Direction.ASC, "id"));

    private final UserRepository userRepository;
    private final AdminUserMapper adminUserMapper;
    private final AdminPageService adminPageService;
    private final CurrentUserService currentUserService;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional(readOnly = true)
    public SearchResponse<AdminUserResponseDto> search(String q,
                                                       UserStatus status,
                                                       UserType type,
                                                       Integer page,
                                                       Integer size,
                                                       String sort) {
        var pageable = adminPageService.pageable(page, size, sort, ALLOWED_SORTS, DEFAULT_SORT);
        var users = userRepository.searchAdminUsers(normalizeQuery(q), status, type, pageable)
                .map(adminUserMapper::toResponseDto);
        return adminPageService.response(users);
    }

    @Transactional(readOnly = true)
    public AdminUserResponseDto getById(UUID id) {
        return userRepository.findById(id)
                .map(adminUserMapper::toResponseDto)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public AdminUserResponseDto updateStatus(UUID id, AdminUserStatusUpdateRequestDto request) {
        UserStatus requestedStatus = request.getStatus();
        if (requestedStatus != UserStatus.active && requestedStatus != UserStatus.disabled) {
            throw new InvalidRequestException("Status must be active or disabled");
        }

        User actor = currentUserService.requireCurrentUser();
        if (actor.getId().equals(id) && requestedStatus == UserStatus.disabled) {
            throw new ForbiddenException("Admins cannot disable their own account");
        }

        User user = userRepository.findByIdForUpdate(id).orElseThrow(UserNotFoundException::new);
        if (user.getStatus() == requestedStatus) {
            return adminUserMapper.toResponseDto(user);
        }

        Map<String, Object> oldData = auditData(AuditData.snapshot(user), "admin user status changed");
        user.setStatus(requestedStatus);
        user.setTokenVersion(user.getTokenVersion() + 1);
        refreshTokenRevoker.revokeAllForUser(user);

        auditEventPublisher.publishUpdateAs(
                actor,
                USERS_TABLE_NAME,
                user.getId(),
                oldData,
                auditData(AuditData.snapshot(user), "admin user status changed"),
                "admin user status changed"
        );

        return adminUserMapper.toResponseDto(user);
    }

    @Transactional
    public AdminUserResponseDto updateType(UUID id, AdminUserTypeUpdateRequestDto request) {
        User actor = currentUserService.requireCurrentUser();
        if (actor.getId().equals(id)) {
            throw new ForbiddenException("Admins cannot change their own account type");
        }

        User user = userRepository.findByIdForUpdate(id).orElseThrow(UserNotFoundException::new);
        UserType requestedType = request.getType();
        if (user.getType() == requestedType) {
            return adminUserMapper.toResponseDto(user);
        }

        Map<String, Object> oldData = auditData(AuditData.snapshot(user), "admin user type changed");
        user.setType(requestedType);
        user.setTokenVersion(user.getTokenVersion() + 1);
        refreshTokenRevoker.revokeAllForUser(user);

        auditEventPublisher.publishUpdateAs(
                actor,
                USERS_TABLE_NAME,
                user.getId(),
                oldData,
                auditData(AuditData.snapshot(user), "admin user type changed"),
                "admin user type changed"
        );

        return adminUserMapper.toResponseDto(user);
    }

    private String normalizeQuery(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return q.trim();
    }

    private Map<String, Object> auditData(Map<String, Object> source, String event) {
        Map<String, Object> data = new LinkedHashMap<>(source);
        data.put("event", event);
        return data;
    }
}
