package com.jamil.ahadith.core.cleanup;

import com.jamil.ahadith.features.account.repository.EmailVerificationTokenRepository;
import com.jamil.ahadith.features.account.repository.PasswordResetTokenRepository;
import com.jamil.ahadith.features.auth.repository.LoginAttemptRepository;
import com.jamil.ahadith.features.auth.repository.RefreshTokenSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SecurityDataCleanupService {
    private final CleanupProperties cleanupProperties;
    private final LoginAttemptRepository loginAttemptRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Scheduled(
            initialDelayString = "${app.cleanup.initial-delay:10m}",
            fixedDelayString = "${app.cleanup.fixed-delay:6h}"
    )

    @Transactional
    public void scheduledCleanup() {
        if (cleanupProperties.isEnabled()) {
            cleanup();
        }
    }

    @Transactional
    public CleanupResult cleanup() {
        Instant tokenCutoff = Instant.now().minus(cleanupProperties.getTokenRetention());
        Instant loginCutoff = Instant.now().minus(cleanupProperties.getLoginAttemptRetention());
        int batchSize = cleanupProperties.getBatchSize();

        int loginAttempts = loginAttemptRepository.deleteOld(loginCutoff, batchSize);
        int refreshSessions = refreshTokenSessionRepository.deleteExpiredBefore(tokenCutoff, batchSize);
        int passwordResetTokens = passwordResetTokenRepository.deleteExpiredOrConsumedBefore(tokenCutoff, batchSize);
        int emailVerificationTokens = emailVerificationTokenRepository.deleteExpiredOrConsumedBefore(tokenCutoff, batchSize);

        return new CleanupResult(loginAttempts, refreshSessions, passwordResetTokens, emailVerificationTokens);
    }

    public record CleanupResult(
            int loginAttempts,
            int refreshTokenSessions,
            int passwordResetTokens,
            int emailVerificationTokens
    ) {
    }
}
