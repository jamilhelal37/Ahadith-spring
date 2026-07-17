package com.jamil.ahadith.features.auth.service;

import com.jamil.ahadith.core.security.RefreshTokenRevoker;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.auth.entity.RefreshTokenSession;
import com.jamil.ahadith.features.account.service.TokenHashService;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.auth.repository.RefreshTokenSessionRepository;
import com.jamil.ahadith.features.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenRevoker {
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenHashService tokenHashService;

    @Transactional
    public String issue(User user, String userAgent, String ipAddress) {
        return issue(user, UUID.randomUUID(), userAgent, ipAddress);
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public RotationResult rotate(String refreshToken, String userAgent, String ipAddress) {
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        Instant now = Instant.now();
        UUID presentedUserId = jwtService.getUserId(refreshToken);
        RefreshTokenSession session = refreshTokenSessionRepository.findByTokenHashForUpdate(tokenHashService.sha256(refreshToken))
                .orElseThrow(() -> {
                    revokePresentedUserSessions(presentedUserId, now);
                    return new BadCredentialsException("Invalid or expired refresh token");
                });

        if (session.getRevokedAt() != null) {
            revokeAllUserSessions(session.getUser(), now);
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
        if (session.getExpiresAt().isBefore(now)) {
            session.setRevokedAt(now);
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
        if (session.getUser().getStatus() != UserStatus.active) {
            refreshTokenSessionRepository.revokeAllForUser(session.getUser(), now);
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        session.setRevokedAt(now);
        String replacement = issue(session.getUser(), session.getFamilyId(), userAgent, ipAddress);
        session.setReplacedByTokenId(jwtService.getTokenId(replacement));
        return new RotationResult(session.getUser(), replacement);
    }

    @Transactional
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            refreshTokenSessionRepository.findByTokenHash(tokenHashService.sha256(refreshToken))
                    .filter(session -> session.getRevokedAt() == null)
                    .ifPresent(session -> session.setRevokedAt(Instant.now()));
        } catch (JwtException | IllegalArgumentException ignored) {
            // Logout is intentionally idempotent.
        }
    }

    @Transactional
    @Override
    public void revokeAllForUser(User user) {
        refreshTokenSessionRepository.revokeAllForUser(user, Instant.now());
    }

    @Transactional
    public int cleanupExpired() {
        return refreshTokenSessionRepository.deleteExpiredBefore(Instant.now());
    }

    private String issue(User user, UUID familyId, String userAgent, String ipAddress) {
        String token = jwtService.generateRefreshToken(user, familyId);
        RefreshTokenSession session = new RefreshTokenSession();
        session.setUser(user);
        session.setTokenHash(tokenHashService.sha256(token));
        session.setTokenId(jwtService.getTokenId(token));
        session.setFamilyId(familyId);
        session.setIssuedAt(Instant.now());
        session.setExpiresAt(jwtService.getExpiresAt(token));
        session.setUserAgent(userAgent);
        session.setIpAddress(ipAddress);
        refreshTokenSessionRepository.save(session);
        return token;
    }

    private void revokePresentedUserSessions(UUID userId, Instant revokedAt) {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> revokeAllUserSessions(user, revokedAt));
        }
    }

    private void revokeAllUserSessions(User user, Instant revokedAt) {
        refreshTokenSessionRepository.revokeAllForUser(user, revokedAt);
        refreshTokenSessionRepository.flush();
    }

    public record RotationResult(User user, String refreshToken) {
    }
}
