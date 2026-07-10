package com.jamil.ahadith.services;

import com.jamil.ahadith.config.SecurityProperties;
import com.jamil.ahadith.entities.LoginAttempt;
import com.jamil.ahadith.exceptions.RateLimitException;
import com.jamil.ahadith.repositories.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;
    private final SecurityProperties securityProperties;

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
        LoginAttempt attempt = loginAttemptRepository.findByEmailKeyAndIpAddress(key(email), ipAddress)
                .orElseGet(() -> {
                    LoginAttempt created = new LoginAttempt();
                    created.setEmailKey(key(email));
                    created.setIpAddress(ipAddress);
                    return created;
                });
        attempt.setFailedCount(attempt.getFailedCount() + 1);
        attempt.setLastFailedAt(Instant.now());
        if (attempt.getFailedCount() >= securityProperties.getLoginMaxFailures()) {
            attempt.setLockedUntil(Instant.now().plus(securityProperties.getLoginLockDuration()));
        }
        loginAttemptRepository.save(attempt);
    }

    @Transactional
    public void recordSuccess(String email, String ipAddress) {
        loginAttemptRepository.findByEmailKeyAndIpAddress(key(email), ipAddress).ifPresent(attempt -> {
            attempt.setFailedCount(0);
            attempt.setLockedUntil(null);
            attempt.setLastFailedAt(null);
            loginAttemptRepository.save(attempt);
        });
    }

    private String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
