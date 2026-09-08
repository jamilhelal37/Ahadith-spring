package com.jamil.ahadith.features.auth;

import com.jamil.ahadith.core.mail.TestEmailService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.features.account.repository.PasswordResetTokenRepository;
import com.jamil.ahadith.features.account.service.TokenHashService;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.account.repository.EmailVerificationTokenRepository;
import com.jamil.ahadith.features.auth.repository.LoginAttemptRepository;
import com.jamil.ahadith.core.ratelimit.RateLimitKeyResolver;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.core.security.jwt.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthWorkflowTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    @Autowired
    private ActivityLogRepository activityLogRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenHashService tokenHashService;
    @Autowired
    private RateLimitKeyResolver rateLimitKeyResolver;
    @Autowired
    private TestEmailService testEmailService;

    @AfterEach
    void clearMail() {
        testEmailService.clear();
    }

    @Test
    void loginShouldRejectDisabledAndPendingUsers() throws Exception {
        createUser("active-status@example.com", UserStatus.active);
        createUser("disabled-status@example.com", UserStatus.disabled);
        createUser("pending-status@example.com", UserStatus.pending_confirmation);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("active-status@example.com", "12345678")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("disabled-status@example.com", "12345678")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account is not active"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("pending-status@example.com", "12345678")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account is not active"));
    }

    @Test
    void disabledUserExistingAccessTokenShouldStopWorking() throws Exception {
        User user = createUser("disabled-token@example.com", UserStatus.active);
        String token = jwtService.generateAccessToken(user);
        user.setStatus(UserStatus.disabled);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtExpirationShouldUseConfiguredDurationSeconds() {
        User user = createUser("duration@example.com", UserStatus.active);
        var claims = jwtService.parseClaims(jwtService.generateAccessToken(user));

        long seconds = Duration.between(claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant()).toSeconds();
        assertThat(seconds).isBetween(3595L, 3605L);
        assertThat(jwtService.getAccessTokenExpirationSeconds()).isEqualTo(3600L);
        assertThat(jwtService.getRefreshTokenExpirationSeconds()).isEqualTo(604800L);
    }

    @Test
    void refreshRotationReplayLogoutAndLogoutAllShouldRevokeSessions() throws Exception {
        createUser("rotation@example.com", UserStatus.active);
        JsonNode login = login("rotation@example.com");

        JsonNode rotated = objectMapper.readTree(mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + login.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + login.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + rotated.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isUnauthorized());

        JsonNode firstSession = login("rotation@example.com");
        JsonNode secondSession = login("rotation@example.com");
        mockMvc.perform(post("/me/logout-all")
                        .header("Authorization", "Bearer " + firstSession.get("accessToken").asText()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + secondSession.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void v1AuthEndpointsShouldIgnoreInvalidAuthorizationHeader() throws Exception {
        String email = unique("v1-ignore-auth");
        createUser(email, UserStatus.active);
        JsonNode login = login(email);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer invalid-expired-access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + login.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("Authorization", "Bearer invalid-expired-access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "12345678")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .header("Authorization", "Bearer invalid-expired-access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointWithInvalidAccessTokenShouldReturnUnifiedErrorResponse() throws Exception {
        mockMvc.perform(get("/me").header("Authorization", "Bearer invalid-expired-access-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/me"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void emailVerificationTokenLifecycleShouldActivateUserOnce() throws Exception {
        String email = unique("verify");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Verify User\",\"email\":\"" + email + "\",\"password\":\"12345678\",\"gender\":\"male\",\"birthDate\":\"2000-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.user.status").value("pending_confirmation"));

        String token = testEmailService.verificationTokenFor(email);
        assertThat(token).isNotBlank();
        assertThat(emailVerificationTokenRepository.findByTokenHash(token)).isEmpty();

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk());
        assertThat(userRepository.findByEmail(email).orElseThrow().getStatus()).isEqualTo(UserStatus.active);

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void disabledUserShouldNotBeReactivatedByEmailVerificationToken() throws Exception {
        String email = unique("verify-disabled");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Verify Disabled\",\"email\":\"" + email + "\",\"password\":\"12345678\",\"gender\":\"male\",\"birthDate\":\"2000-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.status").value("pending_confirmation"));

        String token = testEmailService.verificationTokenFor(email);
        assertThat(token).isNotBlank();

        User user = userRepository.findByEmail(email).orElseThrow();
        user.setStatus(UserStatus.disabled);
        userRepository.saveAndFlush(user);

        var verificationToken = emailVerificationTokenRepository
                .findByTokenHash(tokenHashService.sha256(token))
                .orElseThrow();

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));

        assertThat(userRepository.findByEmail(email).orElseThrow().getStatus())
                .isEqualTo(UserStatus.disabled);
        assertThat(emailVerificationTokenRepository.findById(verificationToken.getId()).orElseThrow().getConsumedAt())
                .isNull();
    }

    @Test
    void expiredEmailVerificationTokenShouldBeRejected() throws Exception {
        String email = unique("verify-expired");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Verify Expired\",\"email\":\"" + email + "\",\"password\":\"12345678\",\"gender\":\"male\",\"birthDate\":\"2000-01-01\"}"))
                .andExpect(status().isOk());

        String token = testEmailService.verificationTokenFor(email);
        var verificationToken = emailVerificationTokenRepository.findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(
                        userRepository.findByEmail(email).orElseThrow())
                .getFirst();
        verificationToken.setExpiresAt(Instant.now().minusSeconds(1));
        emailVerificationTokenRepository.saveAndFlush(verificationToken);

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isUnauthorized());

        assertThat(userRepository.findByEmail(email).orElseThrow().getStatus())
                .isEqualTo(UserStatus.pending_confirmation);
    }

    @Test
    void concurrentRefreshRotationShouldOnlyAllowOneSuccessfulReplacement() throws Exception {
        createUser("refresh-race@example.com", UserStatus.active);
        JsonNode login = login("refresh-race@example.com");
        String refreshToken = login.get("refreshToken").asText();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger unauthorizedCount = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 2; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await(5, TimeUnit.SECONDS);
                        int status = mockMvc.perform(post("/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                                .andReturn().getResponse().getStatus();
                        if (status == 200) {
                            successCount.incrementAndGet();
                        } else if (status == 401) {
                            unauthorizedCount.incrementAndGet();
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(unauthorizedCount.get()).isEqualTo(1);
    }

    @Test
    void resendVerificationShouldSendGenericResponseAndNewTokenAfterThrottleWindow() throws Exception {
        String email = unique("resend");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Resend User\",\"email\":\"" + email + "\",\"password\":\"12345678\",\"gender\":\"male\",\"birthDate\":\"2000-01-01\"}"))
                .andExpect(status().isOk());
        String firstToken = testEmailService.verificationTokenFor(email);
        var token = emailVerificationTokenRepository.findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(
                        userRepository.findByEmail(email).orElseThrow())
                .getFirst();
        token.setLastSentAt(Instant.now().minusSeconds(3600));
        emailVerificationTokenRepository.saveAndFlush(token);

        mockMvc.perform(post("/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email is eligible, a verification message has been sent"));

        assertThat(testEmailService.verificationTokenFor(email)).isNotBlank();
        assertThat(testEmailService.verificationTokenFor(email)).isNotEqualTo(firstToken);
    }
    @Test
    void passwordResetShouldConsumeTokenRevokeSessionsWithoutAuditLog() throws Exception {
        String email = "reset@example.com";
        createUser(email, UserStatus.active);

        JsonNode session = login(email);
        JsonNode secondSession = login(email);

        String oldAccessToken = session.get("accessToken").asText();

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("If the email is registered, password reset instructions have been sent"));

        String token = testEmailService.passwordResetTokenFor(email);

        assertThat(token).isNotBlank();
        assertThat(passwordResetTokenRepository.findByTokenHash(token))
                .isEmpty();

        assertThat(
                passwordResetTokenRepository.findByTokenHash(
                        tokenHashService.sha256(token)
                )
        ).isPresent();

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"token\":\""
                                        + token
                                        + "\",\"newPassword\":\"87654321\"}"
                        ))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + oldAccessToken
                                )
                )
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"refreshToken\":\""
                                        + session.get("refreshToken").asText()
                                        + "\"}"
                        ))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"refreshToken\":\""
                                        + secondSession.get("refreshToken").asText()
                                        + "\"}"
                        ))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"token\":\""
                                        + token
                                        + "\",\"newPassword\":\"87654321\"}"
                        ))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "87654321")))
                .andExpect(status().isOk());

        assertThat(
                activityLogRepository
                        .existsByActorEmailAndMessageContainingIgnoreCase(
                                email,
                                "password reset"
                        )
        ).isFalse();
    }

    @Test
    void issuingNewPasswordResetTokenShouldInvalidatePreviousToken() throws Exception {
        String email = unique("reset-reissue");
        createUser(email, UserStatus.active);

        requestPasswordReset(email);
        String firstToken = testEmailService.passwordResetTokenFor(email);
        assertThat(activePasswordResetTokenCount(email)).isEqualTo(1);

        requestPasswordReset(email);
        String secondToken = testEmailService.passwordResetTokenFor(email);
        assertThat(secondToken).isNotBlank().isNotEqualTo(firstToken);
        assertThat(activePasswordResetTokenCount(email)).isEqualTo(1);

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + firstToken + "\",\"newPassword\":\"87654321\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + secondToken + "\",\"newPassword\":\"87654321\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void expiredPasswordResetTokenShouldBeRejected() throws Exception {
        String email = unique("reset-expired");
        createUser(email, UserStatus.active);

        requestPasswordReset(email);
        String token = testEmailService.passwordResetTokenFor(email);
        var resetToken = passwordResetTokenRepository
                .findByTokenHash(tokenHashService.sha256(token))
                .orElseThrow();
        resetToken.setExpiresAt(Instant.now().minusSeconds(1));
        passwordResetTokenRepository.saveAndFlush(resetToken);

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"87654321\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "12345678")))
                .andExpect(status().isOk());
    }

    @Test
    void concurrentPasswordResetAttemptsShouldNotBothSucceed() throws Exception {
        String email = unique("reset-race");
        createUser(email, UserStatus.active);
        requestPasswordReset(email);
        String token = testEmailService.passwordResetTokenFor(email);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 2; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await(5, TimeUnit.SECONDS);
                        int status = mockMvc.perform(post("/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"87654321\"}"))
                                .andReturn().getResponse().getStatus();
                        if (status == 200) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(activePasswordResetTokenCount(email)).isZero();
    }

    @Test
    void passwordResetEmailFailureShouldNotRollbackTokenChanges() throws Exception {
        String email = unique("reset-rollback");
        createUser(email, UserStatus.active);
        requestPasswordReset(email);
        String firstToken = testEmailService.passwordResetTokenFor(email);
        assertThat(activePasswordResetTokenCount(email)).isEqualTo(1);

        testEmailService.failNextPasswordResetEmail();
        // Should return 200 because email sending failure is after commit and ignored in listener
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email is registered, password reset instructions have been sent"));

        // The second token was saved and committed despite email failure
        assertThat(activePasswordResetTokenCount(email)).isEqualTo(1);
        String secondToken = testEmailService.passwordResetTokenFor(email);
        // The token in email service was not updated because of failure, so it still has the first one
        // OR actually, in our mock, it didn't put the new one because it threw exception.

        // But in the DB, there should be a new one.
        User user = userRepository.findByEmail(email).orElseThrow();
        var tokens = passwordResetTokenRepository.findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(user);
        assertThat(tokens).hasSize(1);

        // The first token should be consumed by issuing the second one
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + firstToken + "\",\"newPassword\":\"87654321\"}"))
                .andExpect(status().isUnauthorized()); // Should be unauthorized because it's consumed/replaced
    }

    @Test
    void forgotPasswordShouldBeGenericForUnknownEmail() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown-" + UUID.randomUUID() + "@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email is registered, password reset instructions have been sent"));
    }

    @Test
    void loginLockoutShouldThrottleAndResetAfterExpiryAndSuccess() throws Exception {
        String email = "lockout@example.com";
        createUser(email, UserStatus.active);

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(email, "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "12345678")))
                .andExpect(status().isTooManyRequests());

        String emailKey = rateLimitKeyResolver.emailHashKey(email);
        var attempt = loginAttemptRepository.findByEmailKeyAndIpAddress(emailKey, "127.0.0.1")
                .orElseThrow();
        attempt.setLockedUntil(Instant.now().minusSeconds(1));
        loginAttemptRepository.saveAndFlush(attempt);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "12345678")))
                .andExpect(status().isOk());

        assertThat(loginAttemptRepository.findByEmailKeyAndIpAddress(emailKey, "127.0.0.1").orElseThrow().getFailedCount())
                .isZero();
    }

    private JsonNode login(String email) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "12345678")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private void requestPasswordReset(String email) throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email is registered, password reset instructions have been sent"));
    }

    private long activePasswordResetTokenCount(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return passwordResetTokenRepository.findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(user).size();
    }

    private User createUser(String email, UserStatus status) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setStatus(status);
        user.setType(UserType.member);
        return userRepository.saveAndFlush(user);
    }

    private String loginJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }
}
