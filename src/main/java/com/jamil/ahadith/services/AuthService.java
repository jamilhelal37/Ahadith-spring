package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.LoginRequestDto;
import com.jamil.ahadith.dtos.requests.RegisterRequestDto;
import com.jamil.ahadith.dtos.requests.ResetPasswordRequestDto;
import com.jamil.ahadith.dtos.responses.AuthResponseDto;
import com.jamil.ahadith.dtos.responses.AuthUserDto;
import com.jamil.ahadith.dtos.responses.MessageResponseDto;
import com.jamil.ahadith.entities.ActivityLog;
import com.jamil.ahadith.entities.EmailVerificationToken;
import com.jamil.ahadith.entities.PasswordResetToken;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.entities.UserStatus;
import com.jamil.ahadith.entities.UserType;
import com.jamil.ahadith.exceptions.ForbiddenException;
import com.jamil.ahadith.exceptions.InvalidRequestException;
import com.jamil.ahadith.exceptions.RateLimitException;
import com.jamil.ahadith.exceptions.UserAlreadyExistsException;
import com.jamil.ahadith.repositories.ActivityLogRepository;
import com.jamil.ahadith.repositories.EmailVerificationTokenRepository;
import com.jamil.ahadith.repositories.PasswordResetTokenRepository;
import com.jamil.ahadith.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenHashService tokenHashService;
    private final EmailService emailService;
    private final com.jamil.ahadith.config.MailConfigProperties mailProperties;
    private final LoginAttemptService loginAttemptService;
    private final PasswordPolicyService passwordPolicyService;
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        passwordPolicyService.validate(request.getPassword());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setType(UserType.member); // Default type
        user.setStatus(UserStatus.pending_confirmation);

        userRepository.save(user);
        createAndSendVerificationToken(user);

        return AuthResponseDto.builder()
                .tokenType("Bearer")
                .expiresIn(0)
                .user(toAuthUserDto(user))
                .build();
    }

    public AuthResponseDto login(LoginRequestDto request, String userAgent, String ipAddress) {
        loginAttemptService.assertNotLocked(request.getEmail(), ipAddress);
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> {
                    loginAttemptService.recordFailure(request.getEmail(), ipAddress);
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailure(request.getEmail(), ipAddress);
            throw new BadCredentialsException("Invalid email or password");
        }
        requireActive(user);
        loginAttemptService.recordSuccess(request.getEmail(), ipAddress);

        return createAuthResponse(user, refreshTokenService.issue(user, userAgent, ipAddress));
    }

    public AuthResponseDto refreshToken(String refreshToken, String userAgent, String ipAddress) {
        var rotation = refreshTokenService.rotate(refreshToken, userAgent, ipAddress);
        requireActive(rotation.user());
        return createAuthResponse(rotation.user(), rotation.refreshToken());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    @Transactional
    public void logoutAll(User user) {
        refreshTokenService.revokeAllForUser(user);
    }

    @Transactional
    public MessageResponseDto verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByTokenHash(tokenHashService.sha256(token))
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired verification token"));
        if (verificationToken.getConsumedAt() != null || verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Invalid or expired verification token");
        }
        verificationToken.setConsumedAt(Instant.now());
        User user = verificationToken.getUser();
        user.setStatus(UserStatus.active);
        userRepository.save(user);
        return new MessageResponseDto("Email verified");
    }

    @Transactional
    public MessageResponseDto resendVerification(String email) {
        userRepository.findByEmail(normalizeEmail(email))
                .filter(user -> user.getStatus() == UserStatus.pending_confirmation)
                .ifPresent(user -> {
                    emailVerificationTokenRepository.findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(user).stream()
                            .findFirst()
                            .filter(token -> token.getLastSentAt().plus(mailProperties.getResendThrottle()).isAfter(Instant.now()))
                            .ifPresent(token -> {
                                throw new RateLimitException("Please wait before requesting another verification email");
                            });
                    createAndSendVerificationToken(user);
                });
        return new MessageResponseDto("If the email is eligible, a verification message has been sent");
    }

    @Transactional
    public MessageResponseDto forgotPassword(String email) {
        userRepository.findByEmail(normalizeEmail(email))
                .filter(user -> user.getStatus() == UserStatus.active)
                .ifPresent(user -> {
                    String token = tokenHashService.generateOpaqueToken();
                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.setUser(user);
                    resetToken.setTokenHash(tokenHashService.sha256(token));
                    resetToken.setExpiresAt(Instant.now().plus(mailProperties.getResetTokenTtl()));
                    passwordResetTokenRepository.save(resetToken);
                    emailService.sendPasswordResetEmail(user, token);
                });
        return new MessageResponseDto("If the email is registered, password reset instructions have been sent");
    }

    @Transactional
    public MessageResponseDto resetPassword(ResetPasswordRequestDto request) {
        passwordPolicyService.validate(request.getNewPassword());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHashService.sha256(request.getToken()))
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired password reset token"));
        if (resetToken.getConsumedAt() != null || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Invalid or expired password reset token");
        }
        User user = resetToken.getUser();
        requireActive(user);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        resetToken.setConsumedAt(Instant.now());
        refreshTokenService.revokeAllForUser(user);
        createSecurityAuditLog(user, "password reset completed");
        userRepository.save(user);
        return new MessageResponseDto("Password has been reset");
    }

    private AuthResponseDto createAuthResponse(User user, String refreshToken) {
        String accessToken = jwtService.generateAccessToken(user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .user(toAuthUserDto(user))
                .build();
    }

    private void createAndSendVerificationToken(User user) {
        String token = tokenHashService.generateOpaqueToken();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setTokenHash(tokenHashService.sha256(token));
        verificationToken.setExpiresAt(Instant.now().plus(mailProperties.getVerificationTokenTtl()));
        verificationToken.setLastSentAt(Instant.now());
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user, token);
    }

    private void requireActive(User user) {
        if (user.getStatus() == UserStatus.disabled) {
            throw new ForbiddenException("Account is not active");
        }
        if (user.getStatus() == UserStatus.pending_confirmation) {
            throw new ForbiddenException("Account is not active");
        }
        if (user.getStatus() != UserStatus.active) {
            throw new ForbiddenException("Account is not active");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private void createSecurityAuditLog(User user, String message) {
        ActivityLog log = new ActivityLog();
        log.setActorUserId(user.getId());
        log.setActorName(user.getName());
        log.setActorEmail(user.getEmail());
        log.setActorAvatarUrl(user.getAvatarUrl());
        log.setMessage(message);
        log.setTableName("users");
        log.setRecordId(user.getId());
        log.setNewData(Map.of("event", message));
        activityLogRepository.save(log);
    }

    private AuthUserDto toAuthUserDto(User user) {
        return AuthUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus() == null ? null : user.getStatus().name())
                .gender(user.getGender() == null ? null : user.getGender().name())
                .type(user.getType() == null ? null : user.getType().name())
                .birthDate(user.getBirthDate())
                .build();
    }
}
