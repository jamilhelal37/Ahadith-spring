package com.jamil.ahadith.features.auth;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.features.auth.dto.request.GoogleLoginRequestDto;
import com.jamil.ahadith.features.auth.dto.response.AuthResponseDto;
import com.jamil.ahadith.features.auth.google.GoogleIdentity;
import com.jamil.ahadith.features.auth.google.GoogleIdentityVerifier;
import com.jamil.ahadith.features.auth.service.AuthService;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleAuthPostgresIT extends PostgresIntegrationTestBase {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FakeGoogleIdentityVerifier googleVerifier;

    @Test
    void concurrentFirstGoogleLoginShouldUsePostgresUniqueConstraintWithoutDuplicateUsers() throws Exception {
        googleVerifier.accept("race-token", new GoogleIdentity(
                "race-google-subject",
                "race-google@example.com",
                true,
                "Race Google",
                null
        ));
        googleVerifier.synchronizeVerifications("race-token", 2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<AuthResponseDto> responses = new CopyOnWriteArrayList<>();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        try (var executor = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 2; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await(5, TimeUnit.SECONDS);
                        responses.add(authService.loginWithGoogle(
                                new GoogleLoginRequestDto("race-token"),
                                "Postgres IT",
                                "127.0.0.1"
                        ));
                    } catch (Throwable ex) {
                        failures.add(ex);
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(failures).isEmpty();
        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(response -> response.getUser().getId())
                .containsOnly(responses.getFirst().getUser().getId());
        assertThat(userRepository.findAll().stream()
                .filter(user -> "race-google-subject".equals(user.getGoogleSubject()))
                .count())
                .isEqualTo(1);
    }

    @TestConfiguration
    static class GoogleAuthPostgresTestConfig {
        @Bean
        @Primary
        FakeGoogleIdentityVerifier fakeGoogleIdentityVerifier() {
            return new FakeGoogleIdentityVerifier();
        }
    }

    static class FakeGoogleIdentityVerifier implements GoogleIdentityVerifier {
        private final Map<String, GoogleIdentity> identities = new ConcurrentHashMap<>();
        private volatile String synchronizedToken;
        private volatile CountDownLatch synchronizedReady;
        private volatile CountDownLatch synchronizedStart;

        @Override
        public GoogleIdentity verify(String idToken) {
            awaitSynchronizedVerification(idToken);
            return identities.get(idToken);
        }

        void accept(String idToken, GoogleIdentity identity) {
            identities.put(idToken, identity);
        }

        void synchronizeVerifications(String idToken, int requests) {
            synchronizedToken = idToken;
            synchronizedReady = new CountDownLatch(requests);
            synchronizedStart = new CountDownLatch(1);
        }

        private void awaitSynchronizedVerification(String idToken) {
            CountDownLatch ready = synchronizedReady;
            CountDownLatch start = synchronizedStart;
            if (!idToken.equals(synchronizedToken)
                    || ready == null
                    || start == null) {
                return;
            }

            ready.countDown();
            try {
                if (ready.await(5, TimeUnit.SECONDS)) {
                    start.countDown();
                }
                start.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex);
            }
        }
    }
}
