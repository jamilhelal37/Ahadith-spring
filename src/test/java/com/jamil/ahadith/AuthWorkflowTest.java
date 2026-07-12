package com.jamil.ahadith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.entities.UserStatus;
import com.jamil.ahadith.entities.UserType;
import com.jamil.ahadith.repositories.ActivityLogRepository;
import com.jamil.ahadith.repositories.EmailVerificationTokenRepository;
import com.jamil.ahadith.repositories.LoginAttemptRepository;
import com.jamil.ahadith.repositories.UserRepository;
import com.jamil.ahadith.services.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
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
    private LoginAttemptRepository loginAttemptRepository;
    @Autowired
    private ActivityLogRepository activityLogRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
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
    void emailVerificationTokenLifecycleShouldActivateUserOnce() throws Exception {
        String email = unique("verify");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Verify User\",\"email\":\"" + email + "\",\"password\":\"12345678\"}"))
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
    void expiredEmailVerificationTokenShouldBeRejected() throws Exception {
        String email = unique("verify-expired");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Verify Expired\",\"email\":\"" + email + "\",\"password\":\"12345678\"}"))
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
                        .content("{\"name\":\"Resend User\",\"email\":\"" + email + "\",\"password\":\"12345678\"}"))
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
    void passwordResetShouldConsumeTokenRevokeSessionsAndWriteAuditLog() throws Exception {
        String email = "reset@example.com";
        createUser(email, UserStatus.active);
        JsonNode session = login(email);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email is registered, password reset instructions have been sent"));

        String token = testEmailService.passwordResetTokenFor(email);
        assertThat(token).isNotBlank();

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"87654321\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + session.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"87654321\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "87654321")))
                .andExpect(status().isOk());

        assertThat(activityLogRepository.existsByActorEmailAndMessageContainingIgnoreCase(email, "password reset"))
                .isTrue();
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

        var attempt = loginAttemptRepository.findByEmailKeyAndIpAddress(email.toLowerCase(Locale.ROOT), "127.0.0.1")
                .orElseThrow();
        attempt.setLockedUntil(Instant.now().minusSeconds(1));
        loginAttemptRepository.saveAndFlush(attempt);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "12345678")))
                .andExpect(status().isOk());

        assertThat(loginAttemptRepository.findByEmailKeyAndIpAddress(email, "127.0.0.1").orElseThrow().getFailedCount())
                .isZero();
    }

    private JsonNode login(String email) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "12345678")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
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
