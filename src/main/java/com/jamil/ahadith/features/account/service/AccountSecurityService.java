package com.jamil.ahadith.features.account.service;

import com.jamil.ahadith.core.config.MailConfigProperties;
import com.jamil.ahadith.features.account.dto.request.ResetPasswordRequestDto;
import com.jamil.ahadith.core.web.dto.MessageResponseDto;
import com.jamil.ahadith.features.audit.entity.ActivityLog;
import com.jamil.ahadith.features.account.entity.EmailVerificationToken;
import com.jamil.ahadith.features.account.entity.PasswordResetToken;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.core.exception.ForbiddenException;
import com.jamil.ahadith.core.exception.RateLimitException;
import com.jamil.ahadith.core.mail.EmailService;
import com.jamil.ahadith.core.security.RefreshTokenRevoker;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.account.repository.EmailVerificationTokenRepository;
import com.jamil.ahadith.features.account.repository.PasswordResetTokenRepository;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountSecurityService {

    private static final String USERS_TABLE_NAME = "users";
    private static final String PASSWORD_RESET_AUDIT_EVENT =
            "password reset completed";
    private static final String ACCOUNT_NOT_ACTIVE_MESSAGE =
            "Account is not active";
    private static final String INVALID_VERIFICATION_TOKEN_MESSAGE =
            "Invalid or expired verification token";
    private static final String INVALID_PASSWORD_RESET_TOKEN_MESSAGE =
            "Invalid or expired password reset token";
    private static final String VERIFICATION_RESEND_MESSAGE =
            "If the email is eligible, a verification message has been sent";
    private static final String FORGOT_PASSWORD_MESSAGE =
            "If the email is registered, password reset instructions have been sent";
    private static final String VERIFICATION_RESEND_LIMIT_MESSAGE =
            "Please wait before requesting another verification email";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenHashService tokenHashService;
    private final EmailService emailService;
    private final MailConfigProperties mailProperties;
    private final PasswordPolicyService passwordPolicyService;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void sendInitialVerification(User user) {
        createAndSendVerificationToken(
                user,
                Instant.now()
        );
    }

    @Transactional
    public MessageResponseDto verifyEmail(String token) {
        Instant now = Instant.now();

        String tokenHash =
                tokenHashService.sha256(token);

        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        INVALID_VERIFICATION_TOKEN_MESSAGE
                                )
                        );

        validateOneTimeToken(
                verificationToken.getConsumedAt(),
                verificationToken.getExpiresAt(),
                now,
                INVALID_VERIFICATION_TOKEN_MESSAGE
        );

        verificationToken.setConsumedAt(now);

        User user = verificationToken.getUser();

        user.setStatus(UserStatus.active);

        emailVerificationTokenRepository.save(
                verificationToken
        );

        userRepository.save(user);

        return new MessageResponseDto(
                "Email verified"
        );
    }

    @Transactional
    public MessageResponseDto resendVerification(
            String email
    ) {
        String normalizedEmail =
                normalizeEmail(email);

        Instant now = Instant.now();

        userRepository.findByEmail(normalizedEmail)
                .filter(this::isPendingConfirmation)
                .ifPresent(user -> resendVerificationToken(user, now));

        return new MessageResponseDto(
                VERIFICATION_RESEND_MESSAGE
        );
    }

    @Transactional
    public MessageResponseDto forgotPassword(
            String email
    ) {
        String normalizedEmail =
                normalizeEmail(email);

        Instant now = Instant.now();

        userRepository.findByEmail(normalizedEmail)
                .filter(this::isActive)
                .ifPresent(user ->
                        createAndSendPasswordResetToken(
                                user,
                                now
                        )
                );

        return new MessageResponseDto(
                FORGOT_PASSWORD_MESSAGE
        );
    }

    @Transactional
    public MessageResponseDto resetPassword(
            ResetPasswordRequestDto request
    ) {
        passwordPolicyService.validate(
                request.getNewPassword()
        );

        Instant now = Instant.now();

        String tokenHash =
                tokenHashService.sha256(
                        request.getToken()
                );

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        INVALID_PASSWORD_RESET_TOKEN_MESSAGE
                                )
                        );

        validateOneTimeToken(
                resetToken.getConsumedAt(),
                resetToken.getExpiresAt(),
                now,
                INVALID_PASSWORD_RESET_TOKEN_MESSAGE
        );

        User user = resetToken.getUser();

        requireActive(user);

        String encodedPassword =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        user.setPassword(encodedPassword);
        resetToken.setConsumedAt(now);

        refreshTokenRevoker.revokeAllForUser(user);

        createPasswordResetAuditLog(user);

        passwordResetTokenRepository.save(resetToken);
        userRepository.save(user);

        return new MessageResponseDto(
                "Password has been reset"
        );
    }

    private void resendVerificationToken(
            User user,
            Instant now
    ) {
        List<EmailVerificationToken> activeTokens =
                emailVerificationTokenRepository
                        .findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(
                                user
                        );

        assertVerificationResendAllowed(
                activeTokens,
                now
        );

        consumeVerificationTokens(
                activeTokens,
                now
        );

        createAndSendVerificationToken(
                user,
                now
        );
    }

    private void createAndSendVerificationToken(
            User user,
            Instant now
    ) {
        String rawToken =
                tokenHashService.generateOpaqueToken();

        EmailVerificationToken verificationToken =
                new EmailVerificationToken();

        verificationToken.setUser(user);

        verificationToken.setTokenHash(
                tokenHashService.sha256(rawToken)
        );

        verificationToken.setExpiresAt(
                now.plus(
                        mailProperties
                                .getVerificationTokenTtl()
                )
        );

        verificationToken.setLastSentAt(now);

        emailVerificationTokenRepository.save(
                verificationToken
        );

        emailService.sendVerificationEmail(
                user,
                rawToken
        );
    }

    private void createAndSendPasswordResetToken(
            User user,
            Instant now
    ) {
        String rawToken =
                tokenHashService.generateOpaqueToken();

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setUser(user);

        resetToken.setTokenHash(
                tokenHashService.sha256(rawToken)
        );

        resetToken.setExpiresAt(
                now.plus(
                        mailProperties.getResetTokenTtl()
                )
        );

        passwordResetTokenRepository.save(
                resetToken
        );

        emailService.sendPasswordResetEmail(
                user,
                rawToken
        );
    }

    private void assertVerificationResendAllowed(
            List<EmailVerificationToken> activeTokens,
            Instant now
    ) {
        if (activeTokens == null
                || activeTokens.isEmpty()) {
            return;
        }

        EmailVerificationToken latestToken =
                activeTokens.getFirst();

        Instant lastSentAt =
                latestToken.getLastSentAt();

        if (lastSentAt == null) {
            return;
        }

        Instant nextAllowedAt =
                lastSentAt.plus(
                        mailProperties.getResendThrottle()
                );

        if (nextAllowedAt.isAfter(now)) {
            throw new RateLimitException(
                    VERIFICATION_RESEND_LIMIT_MESSAGE
            );
        }
    }

    private void consumeVerificationTokens(
            List<EmailVerificationToken> activeTokens,
            Instant consumedAt
    ) {
        if (activeTokens == null
                || activeTokens.isEmpty()) {
            return;
        }

        activeTokens.forEach(token ->
                token.setConsumedAt(consumedAt)
        );

        emailVerificationTokenRepository.saveAll(
                activeTokens
        );
    }

    private void validateOneTimeToken(
            Instant consumedAt,
            Instant expiresAt,
            Instant now,
            String errorMessage
    ) {
        boolean consumed =
                consumedAt != null;

        boolean missingExpiration =
                expiresAt == null;

        boolean expired =
                expiresAt != null
                        && expiresAt.isBefore(now);

        if (consumed
                || missingExpiration
                || expired) {
            throw new BadCredentialsException(
                    errorMessage
            );
        }
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
                && user.getStatus()
                == UserStatus.active;
    }

    private boolean isPendingConfirmation(
            User user
    ) {
        return user != null
                && user.getStatus()
                == UserStatus.pending_confirmation;
    }

    private String normalizeEmail(String email) {
        return email == null
                ? null
                : email.trim()
                  .toLowerCase(Locale.ROOT);
    }

    private void createPasswordResetAuditLog(
            User user
    ) {
        ActivityLog log = new ActivityLog();

        log.setActorUserId(user.getId());
        log.setActorName(user.getName());
        log.setActorEmail(user.getEmail());
        log.setActorAvatarUrl(
                user.getAvatarUrl()
        );
        log.setMessage(
                PASSWORD_RESET_AUDIT_EVENT
        );
        log.setTableName(
                USERS_TABLE_NAME
        );
        log.setRecordId(user.getId());
        log.setNewData(
                Map.of(
                        "event",
                        PASSWORD_RESET_AUDIT_EVENT
                )
        );

        activityLogRepository.save(log);
    }
}
