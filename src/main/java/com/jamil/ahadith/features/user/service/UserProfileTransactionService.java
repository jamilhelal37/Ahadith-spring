package com.jamil.ahadith.features.user.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.core.security.RefreshTokenRevoker;
import com.jamil.ahadith.core.storage.dto.ProfileImageResponse;
import com.jamil.ahadith.core.web.dto.MessageResponseDto;
import com.jamil.ahadith.features.account.service.PasswordPolicyService;
import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.auth.dto.response.AuthUserDto;
import com.jamil.ahadith.features.auth.mapper.AuthUserMapper;
import com.jamil.ahadith.features.user.dto.request.ChangePasswordRequestDto;
import com.jamil.ahadith.features.user.dto.request.UserProfileUpdateRequestDto;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.exception.UserNotFoundException;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileTransactionService {
    private static final String USERS_TABLE_NAME = "users";

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthUserMapper authUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional
    public void replaceProfileImage(UUID userId, ProfileImageResponse uploadedImage) {
        User user = userRepository.findByIdForUpdate(userId).orElseThrow(UserNotFoundException::new);
        String oldPublicId = user.getAvatarPublicId();
        String newPublicId = uploadedImage.getAvatarPublicId();
        user.setAvatarUrl(uploadedImage.getAvatarUrl());
        user.setAvatarPublicId(newPublicId);
        userRepository.save(user);
        eventPublisher.publishEvent(new ProfileImageChangedEvent(oldPublicId, newPublicId));
    }

    @Transactional
    public void clearProfileImage(UUID userId) {
        User user = userRepository.findByIdForUpdate(userId).orElseThrow(UserNotFoundException::new);
        String oldPublicId = user.getAvatarPublicId();
        user.setAvatarUrl(null);
        user.setAvatarPublicId(null);
        userRepository.save(user);
        eventPublisher.publishEvent(new ProfileImageChangedEvent(oldPublicId, null));
    }

    @Transactional
    public AuthUserDto updateProfile(UUID userId, UserProfileUpdateRequestDto request) {
        User user = userRepository.findByIdForUpdate(userId).orElseThrow(UserNotFoundException::new);
        Map<String, Object> oldData = auditData(AuditData.snapshot(user), "profile updated");

        user.setName(request.getName().trim());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());

        auditEventPublisher.publishUpdateAs(
                user,
                USERS_TABLE_NAME,
                user.getId(),
                oldData,
                auditData(AuditData.snapshot(user), "profile updated"),
                "profile updated"
        );

        return authUserMapper.toDto(user);
    }

    @Transactional
    public MessageResponseDto changePassword(UUID userId, ChangePasswordRequestDto request) {
        passwordPolicyService.validate(request.getNewPassword());

        User user = userRepository.findByIdForUpdate(userId).orElseThrow(UserNotFoundException::new);
        Map<String, Object> oldData = auditData(AuditData.snapshot(user), "password changed");

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        refreshTokenRevoker.revokeAllForUser(user);

        auditEventPublisher.publishUpdateAs(
                user,
                USERS_TABLE_NAME,
                user.getId(),
                oldData,
                auditData(AuditData.snapshot(user), "password changed"),
                "password changed"
        );

        return new MessageResponseDto("Password changed successfully");
    }

    private Map<String, Object> auditData(Map<String, Object> source, String event) {
        Map<String, Object> data = new LinkedHashMap<>(source);
        data.put("event", event);
        return data;
    }
}
