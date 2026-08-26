package com.jamil.ahadith.features.auth.service;

import com.jamil.ahadith.core.validation.EmailNormalizer;
import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.account.service.EmailVerificationService;

import com.jamil.ahadith.core.security.jwt.JwtService;

import com.jamil.ahadith.features.account.service.PasswordPolicyService;

import com.jamil.ahadith.features.auth.dto.request.GoogleLoginRequestDto;
import com.jamil.ahadith.features.auth.dto.request.LoginRequestDto;
import com.jamil.ahadith.features.auth.dto.request.RegisterRequestDto;
import com.jamil.ahadith.features.auth.dto.response.AuthResponseDto;
import com.jamil.ahadith.features.auth.google.GoogleIdentity;
import com.jamil.ahadith.features.auth.google.GoogleIdentityVerifier;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.core.exception.ConflictException;
import com.jamil.ahadith.core.exception.ForbiddenException;
import com.jamil.ahadith.core.exception.UserAlreadyExistsException;
import com.jamil.ahadith.core.ratelimit.RateLimitKeyResolver;
import com.jamil.ahadith.core.ratelimit.RateLimitService;
import com.jamil.ahadith.features.account.repository.EmailVerificationTokenRepository;
import com.jamil.ahadith.features.auth.mapper.AuthUserMapper;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final String INVALID_CREDENTIALS_MESSAGE =
            "Invalid email or password";
    private static final String INVALID_GOOGLE_ID_TOKEN_MESSAGE =
            "Invalid Google ID token";
    private static final String ACCOUNT_NOT_ACTIVE_MESSAGE =
            "Account is not active";
    private static final String GOOGLE_IDENTITY_CONFLICT_MESSAGE =
            "Google identity is already linked to a different account";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordPolicyService passwordPolicyService;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AuthUserMapper authUserMapper;
    private final RateLimitService rateLimitService;
    private final RateLimitKeyResolver rateLimitKeyResolver;
    private final GoogleIdentityVerifier googleIdentityVerifier;
    private final TransactionTemplate transactionTemplate;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        passwordPolicyService.validate(request.getPassword());

        String email = normalizeEmail(request.getEmail());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(
                    "Email is already registered"
            );
        }

        User user = createPendingUser(request, email);

        userRepository.save(user);

        emailVerificationService.sendInitialVerification(user);

        return AuthResponseDto.builder()
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(0)
                .user(authUserMapper.toDto(user))
                .build();
    }

    public AuthResponseDto login(
            LoginRequestDto request,
            String userAgent,
            String ipAddress
    ) {
        String email = normalizeEmail(
                request.getEmail()
        );

        rateLimitService.assertAllowed(
                "login-email",
                rateLimitKeyResolver.emailHashKey(email)
        );

        loginAttemptService.assertNotLocked(
                email,
                ipAddress
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        invalidCredentials(
                                email,
                                ipAddress
                        )
                );

        boolean passwordMatches =
                user.getPassword() != null
                        && passwordEncoder.matches(
                                request.getPassword(),
                                user.getPassword()
                        );

        if (!passwordMatches) {
            throw invalidCredentials(
                    email,
                    ipAddress
            );
        }

        requireActive(user);

        loginAttemptService.recordSuccess(
                email,
                ipAddress
        );

        String refreshToken =
                refreshTokenService.issue(
                        user,
                        userAgent,
                        ipAddress
                );

        return createAuthResponse(
                user,
                refreshToken
        );
    }

    public AuthResponseDto loginWithGoogle(
            GoogleLoginRequestDto request,
            String userAgent,
            String ipAddress
    ) {
        GoogleIdentity identity =
                validateGoogleIdentity(
                        googleIdentityVerifier.verify(request.getIdToken())
                );

        try {
            return executeGoogleLoginTransaction(
                    identity,
                    userAgent,
                    ipAddress
            );
        } catch (DataIntegrityViolationException ex) {
            return recoverConcurrentGoogleLogin(
                    identity,
                    userAgent,
                    ipAddress,
                    ex
            );
        }
    }

    private AuthResponseDto executeGoogleLoginTransaction(
            GoogleIdentity identity,
            String userAgent,
            String ipAddress
    ) {
        return Objects.requireNonNull(
                transactionTemplate.execute(status ->
                        completeGoogleLogin(
                                identity,
                                userAgent,
                                ipAddress
                        )
                )
        );
    }

    private AuthResponseDto recoverConcurrentGoogleLogin(
            GoogleIdentity identity,
            String userAgent,
            String ipAddress,
            DataIntegrityViolationException originalException
    ) {
        return Objects.requireNonNull(
                transactionTemplate.execute(status ->
                        completeExistingGoogleLoginAfterConflict(
                                identity,
                                userAgent,
                                ipAddress,
                                originalException
                        )
                )
        );
    }

    private AuthResponseDto completeExistingGoogleLoginAfterConflict(
            GoogleIdentity identity,
            String userAgent,
            String ipAddress,
            DataIntegrityViolationException originalException
    ) {
        User user = userRepository.findByGoogleSubjectForUpdate(
                        identity.subject()
                )
                .map(existingUser ->
                        updateGoogleAvatarIfAllowed(
                                existingUser,
                                identity
                        )
                )
                .orElseThrow(() -> originalException);

        requireActive(user);

        String refreshToken =
                refreshTokenService.issue(
                        user,
                        userAgent,
                        ipAddress
                );

        return createAuthResponse(
                user,
                refreshToken
        );
    }

    private AuthResponseDto completeGoogleLogin(
            GoogleIdentity identity,
            String userAgent,
            String ipAddress
    ) {
        User user = findOrCreateGoogleUser(identity);

        requireActive(user);

        String refreshToken =
                refreshTokenService.issue(
                        user,
                        userAgent,
                        ipAddress
                );

        return createAuthResponse(
                user,
                refreshToken
        );
    }

    public AuthResponseDto refreshToken(
            String refreshToken,
            String userAgent,
            String ipAddress
    ) {
        var rotation = refreshTokenService.rotate(
                refreshToken,
                userAgent,
                ipAddress
        );

        User user = rotation.user();

        requireActive(user);

        return createAuthResponse(
                user,
                rotation.refreshToken()
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    @Transactional
    public void logoutAll(User user) {
        refreshTokenService.revokeAllForUser(user);
    }

    private User createPendingUser(
            RegisterRequestDto request,
            String normalizedEmail
    ) {
        User user = new User();

        user.setName(request.getName());
        user.setEmail(normalizedEmail);

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());
        user.setType(UserType.member);
        user.setStatus(
                UserStatus.pending_confirmation
        );

        return user;
    }

    private AuthResponseDto createAuthResponse(
            User user,
            String refreshToken
    ) {
        String accessToken =
                jwtService.generateAccessToken(user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(
                        jwtService
                                .getAccessTokenExpirationSeconds()
                )
                .user(authUserMapper.toDto(user))
                .build();
    }

    private User findOrCreateGoogleUser(GoogleIdentity identity) {
        return userRepository.findByGoogleSubjectForUpdate(identity.subject())
                .map(user -> updateGoogleAvatarIfAllowed(user, identity))
                .orElseGet(() -> linkOrCreateGoogleUser(identity));
    }

    private User linkOrCreateGoogleUser(GoogleIdentity identity) {
        return userRepository.findByEmailForUpdate(identity.email())
                .map(existingUser -> linkGoogleSubject(existingUser, identity))
                .orElseGet(() -> createGoogleUser(identity));
    }

    private User linkGoogleSubject(
            User user,
            GoogleIdentity identity
    ) {
        if (user.getStatus() == UserStatus.disabled) {
            throw new ForbiddenException(ACCOUNT_NOT_ACTIVE_MESSAGE);
        }

        String existingGoogleSubject = trimToNull(user.getGoogleSubject());
        if (existingGoogleSubject != null
                && !existingGoogleSubject.equals(identity.subject())) {
            throw new ConflictException(GOOGLE_IDENTITY_CONFLICT_MESSAGE);
        }

        user.setGoogleSubject(identity.subject());

        if (user.getStatus() == UserStatus.pending_confirmation) {
            user.setStatus(UserStatus.active);
            emailVerificationTokenRepository.consumeActiveForUser(
                    user,
                    Instant.now()
            );
        }

        return updateGoogleAvatarIfAllowed(user, identity);
    }

    private User createGoogleUser(GoogleIdentity identity) {
        User user = new User();
        user.setGoogleSubject(identity.subject());
        user.setEmail(identity.email());
        user.setName(safeGoogleName(identity));
        user.setAvatarUrl(trimToNull(identity.pictureUrl()));
        user.setAvatarPublicId(null);
        user.setPassword(null);
        user.setStatus(UserStatus.active);
        user.setType(UserType.member);
        user.setGender(null);
        user.setBirthDate(null);
        user.setTokenVersion(0);

        return userRepository.saveAndFlush(user);
    }

    private User updateGoogleAvatarIfAllowed(
            User user,
            GoogleIdentity identity
    ) {
        if (trimToNull(user.getAvatarUrl()) == null
                && trimToNull(user.getAvatarPublicId()) == null
                && trimToNull(identity.pictureUrl()) != null) {
            user.setAvatarUrl(identity.pictureUrl().trim());
        }
        return user;
    }

    private GoogleIdentity validateGoogleIdentity(GoogleIdentity identity) {
        if (identity == null
                || trimToNull(identity.subject()) == null
                || trimToNull(identity.email()) == null
                || !identity.emailVerified()) {
            throw new BadCredentialsException(INVALID_GOOGLE_ID_TOKEN_MESSAGE);
        }

        String normalizedEmail = normalizeEmail(identity.email());
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new BadCredentialsException(INVALID_GOOGLE_ID_TOKEN_MESSAGE);
        }

        return new GoogleIdentity(
                identity.subject().trim(),
                normalizedEmail,
                true,
                trimToNull(identity.name()),
                trimToNull(identity.pictureUrl())
        );
    }

    private String safeGoogleName(GoogleIdentity identity) {
        String name = trimToNull(identity.name());
        if (name == null) {
            name = safeNameFromEmail(identity.email());
        }

        if (name.length() <= ValidationLimits.NAME_MAX) {
            return name;
        }
        return name.substring(0, ValidationLimits.NAME_MAX);
    }

    private String safeNameFromEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        int atIndex = normalizedEmail == null ? -1 : normalizedEmail.indexOf('@');
        String localPart = atIndex > 0
                ? normalizedEmail.substring(0, atIndex)
                : "google-user";
        String candidate = localPart
                .replaceAll("[^A-Za-z0-9._ -]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return candidate.isBlank() ? "google-user" : candidate;
    }

    private BadCredentialsException invalidCredentials(
            String email,
            String ipAddress
    ) {
        loginAttemptService.recordFailure(
                email,
                ipAddress
        );

        return new BadCredentialsException(
                INVALID_CREDENTIALS_MESSAGE
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
                && user.getStatus()
                == UserStatus.active;
    }

    private String normalizeEmail(String email) {
        return EmailNormalizer.normalize(email);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
