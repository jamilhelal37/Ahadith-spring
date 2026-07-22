package com.jamil.ahadith.features.auth.service;

import com.jamil.ahadith.features.account.service.AccountSecurityService;

import com.jamil.ahadith.core.security.jwt.JwtService;

import com.jamil.ahadith.features.account.service.PasswordPolicyService;

import com.jamil.ahadith.features.auth.dto.request.LoginRequestDto;
import com.jamil.ahadith.features.auth.dto.request.RegisterRequestDto;
import com.jamil.ahadith.features.auth.dto.response.AuthResponseDto;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.core.exception.ForbiddenException;
import com.jamil.ahadith.core.exception.UserAlreadyExistsException;
import com.jamil.ahadith.core.ratelimit.RateLimitKeyResolver;
import com.jamil.ahadith.core.ratelimit.RateLimitService;
import com.jamil.ahadith.features.auth.mapper.AuthUserMapper;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final String INVALID_CREDENTIALS_MESSAGE =
            "Invalid email or password";
    private static final String ACCOUNT_NOT_ACTIVE_MESSAGE =
            "Account is not active";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordPolicyService passwordPolicyService;
    private final AccountSecurityService accountSecurityService;
    private final AuthUserMapper authUserMapper;
    private final RateLimitService rateLimitService;
    private final RateLimitKeyResolver rateLimitKeyResolver;

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

        accountSecurityService.sendInitialVerification(user);

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
                passwordEncoder.matches(
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
        user.setAvatarUrl(request.getAvatarUrl());
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
        return email == null
                ? null
                : email.trim()
                  .toLowerCase(Locale.ROOT);
    }
}
