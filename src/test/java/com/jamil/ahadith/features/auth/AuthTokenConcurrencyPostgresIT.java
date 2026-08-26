package com.jamil.ahadith.features.auth;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.features.account.dto.request.ResetPasswordRequestDto;
import com.jamil.ahadith.features.account.entity.PasswordResetToken;
import com.jamil.ahadith.features.account.repository.PasswordResetTokenRepository;
import com.jamil.ahadith.features.account.service.PasswordResetService;
import com.jamil.ahadith.features.account.service.TokenHashService;
import com.jamil.ahadith.features.auth.service.RefreshTokenService;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTokenConcurrencyPostgresIT extends PostgresIntegrationTestBase {
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private TokenHashService tokenHashService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void concurrentRefreshTokenRotationShouldBeSerializedByPostgresForUpdateLock() throws Exception {
        User user = user("refresh-lock@example.com");
        String refreshToken = refreshTokenService.issue(user, "Postgres IT", "127.0.0.1");
        List<RefreshTokenService.RotationResult> successes = new CopyOnWriteArrayList<>();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        runConcurrently(2, () -> {
            try {
                successes.add(refreshTokenService.rotate(refreshToken, "Postgres IT", "127.0.0.1"));
            } catch (Throwable ex) {
                failures.add(ex);
            }
        });

        assertThat(successes).hasSize(1);
        assertThat(failures).hasSize(1);
        assertThat(failures.getFirst()).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void concurrentPasswordResetShouldBeSerializedByPostgresForUpdateLock() throws Exception {
        User user = user("password-reset-lock@example.com");
        String rawToken = tokenHashService.generateOpaqueToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(tokenHashService.sha256(rawToken));
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        passwordResetTokenRepository.saveAndFlush(token);
        List<String> successes = new CopyOnWriteArrayList<>();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        runConcurrently(2, () -> {
            try {
                passwordResetService.resetPassword(resetRequest(rawToken));
                successes.add("ok");
            } catch (Throwable ex) {
                failures.add(ex);
            }
        });

        assertThat(successes).hasSize(1);
        assertThat(failures).hasSize(1);
        assertThat(failures.getFirst()).isInstanceOf(BadCredentialsException.class);
        assertThat(passwordResetTokenRepository.findById(token.getId()).orElseThrow().getConsumedAt())
                .isNotNull();
    }

    private void runConcurrently(int workers, Runnable task) throws Exception {
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(workers)) {
            for (int i = 0; i < workers; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await(5, TimeUnit.SECONDS);
                        task.run();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ex);
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    private ResetPasswordRequestDto resetRequest(String rawToken) {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setToken(rawToken);
        request.setNewPassword("87654321");
        return request;
    }

    private User user(String email) {
        User user = new User();
        user.setName("Postgres Lock User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setStatus(UserStatus.active);
        user.setType(UserType.member);
        return userRepository.saveAndFlush(user);
    }
}
