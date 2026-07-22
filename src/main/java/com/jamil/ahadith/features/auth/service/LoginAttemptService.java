package com.jamil.ahadith.features.auth.service;

import com.jamil.ahadith.core.config.SecurityProperties;
import com.jamil.ahadith.core.exception.RateLimitException;
import com.jamil.ahadith.core.ratelimit.RateLimitKeyResolver;
import com.jamil.ahadith.features.auth.entity.LoginAttempt;
import com.jamil.ahadith.features.auth.repository.LoginAttemptRepository;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;
    private final SecurityProperties securityProperties;
    private final RateLimitKeyResolver rateLimitKeyResolver;
    private final DataSource dataSource;

    @Transactional(readOnly = true)
    public void assertNotLocked(String email, String ipAddress) {
        loginAttemptRepository.findByEmailKeyAndIpAddress(key(email), ipAddress)
                .filter(attempt -> attempt.getLockedUntil() != null && attempt.getLockedUntil().isAfter(Instant.now()))
                .ifPresent(attempt -> {
                    throw new RateLimitException("Too many failed login attempts. Try again later.");
                });
    }

    @Transactional
    public void recordFailure(String email, String ipAddress) {
        Instant now = Instant.now();
        if (isH2()) {
            recordFailurePortable(email, ipAddress, now);
            return;
        }
        loginAttemptRepository.recordFailure(
                key(email),
                ipAddress,
                securityProperties.getLoginMaxFailures(),
                now,
                now.plus(securityProperties.getLoginLockDuration())
        );
    }

    @Transactional
    public void recordSuccess(String email, String ipAddress) {
        loginAttemptRepository.recordSuccess(key(email), ipAddress);
    }

    private String key(String email) {
        return rateLimitKeyResolver.emailHashKey(email);
    }

    private void recordFailurePortable(String email, String ipAddress, Instant now) {
        LoginAttempt attempt = loginAttemptRepository.findByEmailKeyAndIpAddress(key(email), ipAddress)
                .orElseGet(() -> {
                    LoginAttempt created = new LoginAttempt();
                    created.setEmailKey(key(email));
                    created.setIpAddress(ipAddress);
                    return created;
                });
        attempt.setFailedCount(attempt.getFailedCount() + 1);
        attempt.setLastFailedAt(now);
        if (attempt.getFailedCount() >= securityProperties.getLoginMaxFailures()) {
            attempt.setLockedUntil(now.plus(securityProperties.getLoginLockDuration()));
        }
        loginAttemptRepository.save(attempt);
    }

    private boolean isH2() {
        try (var connection = dataSource.getConnection()) {
            return "H2".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException ex) {
            return false;
        }
    }
}
