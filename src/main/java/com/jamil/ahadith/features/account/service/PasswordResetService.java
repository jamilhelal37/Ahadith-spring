package com.jamil.ahadith.features.account.service;

import com.jamil.ahadith.core.config.MailConfigProperties;
import com.jamil.ahadith.core.exception.ForbiddenException;
import com.jamil.ahadith.core.ratelimit.RateLimitKeyResolver;
import com.jamil.ahadith.core.ratelimit.RateLimitService;
import com.jamil.ahadith.core.security.RefreshTokenRevoker;
import com.jamil.ahadith.core.validation.EmailNormalizer;
import com.jamil.ahadith.core.web.dto.MessageResponseDto;
import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.account.dto.request.ResetPasswordRequestDto;
import com.jamil.ahadith.features.account.entity.PasswordResetToken;
import com.jamil.ahadith.features.account.event.AccountEmailEvent;
import com.jamil.ahadith.features.account.repository.PasswordResetTokenRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String ACCOUNT_NOT_ACTIVE_MESSAGE = "Account is not active";
    private static final String INVALID_PASSWORD_RESET_TOKEN_MESSAGE = "Invalid or expired password reset token";
    private static final String FORGOT_PASSWORD_MESSAGE =
            "If the email is registered, password reset instructions have been sent";
    private static final String USERS_TABLE_NAME = "users";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenHashService tokenHashService;
    private final MailConfigProperties mailProperties;
    private final PasswordPolicyService passwordPolicyService;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final RateLimitService rateLimitService;
    private final RateLimitKeyResolver rateLimitKeyResolver;
    private final OneTimeTokenValidator oneTimeTokenValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional
    public MessageResponseDto forgotPassword(String email) {
        String normalizedEmail = EmailNormalizer.normalize(email);

        rateLimitService.assertAllowed(
                "forgot-password-email",
                rateLimitKeyResolver.emailHashKey(normalizedEmail)
        );

        Instant now = Instant.now();

        userRepository.findByEmailForUpdate(normalizedEmail)
                .filter(this::isActive)
                .ifPresent(user -> createAndSendPasswordResetToken(user, now));

        return new MessageResponseDto(FORGOT_PASSWORD_MESSAGE);
    }

    @Transactional
    public MessageResponseDto resetPassword(ResetPasswordRequestDto request) {
        passwordPolicyService.validate(request.getNewPassword());

        Instant now = Instant.now();
        String tokenHash = tokenHashService.sha256(request.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                INVALID_PASSWORD_RESET_TOKEN_MESSAGE
                        )
                );

        oneTimeTokenValidator.validate(
                resetToken.getConsumedAt(),
                resetToken.getExpiresAt(),
                now,
                INVALID_PASSWORD_RESET_TOKEN_MESSAGE
        );

        User user = resetToken.getUser();
        requireActive(user);
        var oldData = AuditData.snapshot(user);

        String encodedPassword =
                passwordEncoder.encode(request.getNewPassword());

        user.setPassword(encodedPassword);
        user.setTokenVersion(user.getTokenVersion() + 1);

        resetToken.setConsumedAt(now);

        refreshTokenRevoker.revokeAllForUser(user);

        passwordResetTokenRepository.consumeActiveForUser(user, now);

        auditEventPublisher.publishUpdateAs(
                user,
                USERS_TABLE_NAME,
                user.getId(),
                oldData,
                AuditData.snapshot(user),
                "password reset"
        );

        return new MessageResponseDto(
                "Password has been reset"
        );
    }

    private void createAndSendPasswordResetToken(
            User user,
            Instant now
    ) {
        passwordResetTokenRepository.consumeActiveForUser(user, now);

        String rawToken =
                tokenHashService.generateOpaqueToken();

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setUser(user);
        resetToken.setTokenHash(
                tokenHashService.sha256(rawToken)
        );
        resetToken.setExpiresAt(
                now.plus(mailProperties.getResetTokenTtl())
        );

        passwordResetTokenRepository.save(resetToken);

        eventPublisher.publishEvent(
                new AccountEmailEvent(
                        user,
                        rawToken,
                        AccountEmailEvent.EmailEventType.PASSWORD_RESET
                )
        );
    }

    private void requireActive(User user) {
        if (!isActive(user)) {
            throw new ForbiddenException(
                    ACCOUNT_NOT_ACTIVE_MESSAGE
            );
        }
    }

    private boolean isActive(User user) {
        return user != null
                && user.getStatus() == UserStatus.active;
    }
}