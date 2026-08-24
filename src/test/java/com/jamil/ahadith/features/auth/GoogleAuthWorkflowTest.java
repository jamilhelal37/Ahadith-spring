package com.jamil.ahadith.features.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.features.account.entity.EmailVerificationToken;
import com.jamil.ahadith.features.account.repository.EmailVerificationTokenRepository;
import com.jamil.ahadith.features.account.service.TokenHashService;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.auth.google.GoogleIdentity;
import com.jamil.ahadith.features.auth.google.GoogleIdentityVerifier;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.google-auth.enabled=true",
        "app.google-auth.client-ids=test-web-client-id"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleAuthWorkflowTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Autowired
    private ActivityLogRepository activityLogRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenHashService tokenHashService;
    @Autowired
    private FakeGoogleIdentityVerifier googleVerifier;

    @AfterEach
    void cleanup() {
        googleVerifier.clear();
        activityLogRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void validGoogleIdTokenShouldCreateActiveMemberAndIssueProjectTokens() throws Exception {
        googleVerifier.accept(
                "valid-new-token",
                identity("google-sub-new", "Google.User@example.com", true, "Google User", "https://example.com/a.png")
        );

        JsonNode response = googleLogin("valid-new-token");

        assertThat(response.get("accessToken").asText()).isNotBlank();
        assertThat(response.get("refreshToken").asText()).isNotBlank();
        assertThat(response.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(response.get("expiresIn").asLong()).isEqualTo(3600L);
        assertThat(response.toString())
                .doesNotContain("googleSubject")
                .doesNotContain("password")
                .doesNotContain("valid-new-token");

        User user = userRepository.findByEmail("google.user@example.com").orElseThrow();
        assertThat(user.getGoogleSubject()).isEqualTo("google-sub-new");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.active);
        assertThat(user.getType()).isEqualTo(UserType.member);
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/a.png");
    }

    @Test
    void repeatedGoogleLoginShouldReturnSameUserWithoutDuplicates() throws Exception {
        googleVerifier.accept("same-sub-token", identity("same-sub", "same@example.com", true, "Same User", null));

        JsonNode first = googleLogin("same-sub-token");
        JsonNode second = googleLogin("same-sub-token");

        assertThat(second.at("/user/id").asText()).isEqualTo(first.at("/user/id").asText());
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void localAccountWithSameVerifiedEmailShouldBeLinkedAndPreservePasswordAndType() throws Exception {
        User local = createUser("linked@example.com", UserStatus.active, UserType.scholar, "12345678");
        String oldPassword = local.getPassword();
        googleVerifier.accept("link-token", identity("linked-sub", "linked@example.com", true, "Ignored Name", null));

        googleLogin("link-token");

        User linked = userRepository.findByEmail("linked@example.com").orElseThrow();
        assertThat(linked.getId()).isEqualTo(local.getId());
        assertThat(linked.getGoogleSubject()).isEqualTo("linked-sub");
        assertThat(linked.getPassword()).isEqualTo(oldPassword);
        assertThat(linked.getType()).isEqualTo(UserType.scholar);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("linked@example.com", "12345678")))
                .andExpect(status().isOk());
    }

    @Test
    void pendingConfirmationAccountShouldBecomeActiveAndConsumeVerificationTokens() throws Exception {
        User pending = createUser("pending-google@example.com", UserStatus.pending_confirmation, UserType.member, "12345678");
        EmailVerificationToken token = activeVerificationToken(pending);
        googleVerifier.accept("pending-token", identity("pending-sub", pending.getEmail(), true, "Pending User", null));

        googleLogin("pending-token");

        User linked = userRepository.findByEmail(pending.getEmail()).orElseThrow();
        assertThat(linked.getStatus()).isEqualTo(UserStatus.active);
        assertThat(linked.getGoogleSubject()).isEqualTo("pending-sub");
        assertThat(emailVerificationTokenRepository.findById(token.getId()).orElseThrow().getConsumedAt())
                .isNotNull();
    }

    @Test
    void disabledAccountShouldNotBeLinkedOrReactivated() throws Exception {
        User disabled = createUser("disabled-google@example.com", UserStatus.disabled, UserType.member, "12345678");
        googleVerifier.accept("disabled-token", identity("disabled-sub", disabled.getEmail(), true, "Disabled User", null));

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleJson("disabled-token")))
                .andExpect(status().isForbidden());

        User reloaded = userRepository.findByEmail(disabled.getEmail()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(UserStatus.disabled);
        assertThat(reloaded.getGoogleSubject()).isNull();
    }

    @Test
    void differentGoogleSubjectForSameEmailShouldReturnConflict() throws Exception {
        User local = createUser("conflict-google@example.com", UserStatus.active, UserType.member, "12345678");
        local.setGoogleSubject("existing-sub");
        userRepository.saveAndFlush(local);
        googleVerifier.accept("conflict-token", identity("different-sub", local.getEmail(), true, "Conflict User", null));

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleJson("conflict-token")))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidUnverifiedOrIncompleteGoogleIdentityShouldReturnUnauthorized() throws Exception {
        googleVerifier.reject("invalid-token");
        googleVerifier.accept("unverified-token", identity("unverified-sub", "unverified@example.com", false, "User", null));
        googleVerifier.accept("blank-sub-token", identity(" ", "blank-sub@example.com", true, "User", null));
        googleVerifier.accept("blank-email-token", identity("blank-email-sub", " ", true, "User", null));

        assertUnauthorizedGoogleLogin("invalid-token");
        assertUnauthorizedGoogleLogin("unverified-token");
        assertUnauthorizedGoogleLogin("blank-sub-token");
        assertUnauthorizedGoogleLogin("blank-email-token");
    }

    @Test
    void googleOnlyUserShouldNotBreakTraditionalLoginAndTraditionalLoginStillWorks() throws Exception {
        googleVerifier.accept("google-only-token", identity("google-only-sub", "google-only@example.com", true, "Google Only", null));
        googleLogin("google-only-token");
        createUser("traditional@example.com", UserStatus.active, UserType.member, "12345678");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("google-only@example.com", "12345678")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("traditional@example.com", "12345678")))
                .andExpect(status().isOk());
    }

    @Test
    void refreshTokenRotationShouldStillWorkForGoogleLogin() throws Exception {
        googleVerifier.accept("rotate-google-token", identity("rotate-google-sub", "rotate-google@example.com", true, "Rotate User", null));
        JsonNode login = googleLogin("rotate-google-token");

        JsonNode rotated = objectMapper.readTree(mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + login.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(rotated.get("refreshToken").asText()).isNotBlank();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + login.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void googleLoginShouldNotWriteGoogleTokenOrSubjectToResponseOrAuditLog() throws Exception {
        String idToken = "sensitive-google-id-token";
        String subject = "sensitive-google-subject";
        googleVerifier.accept(idToken, identity(subject, "audit-google@example.com", true, "Audit User", null));

        JsonNode response = googleLogin(idToken);

        assertThat(response.toString())
                .doesNotContain(idToken)
                .doesNotContain(subject)
                .doesNotContain("googleSubject")
                .doesNotContain("password");
        assertThat(activityLogRepository.findAll())
                .allSatisfy(log -> assertThat(log.toString())
                        .doesNotContain(idToken)
                        .doesNotContain(subject));
    }

    @Test
    void concurrentGoogleLoginShouldNotCreateDuplicateUsersForSameIdentity() throws Exception {
        googleVerifier.accept("race-token", identity("race-sub", "race@example.com", true, "Race User", null));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 2; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await(5, TimeUnit.SECONDS);
                        int status = mockMvc.perform(post("/auth/google")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(googleJson("race-token")))
                                .andReturn().getResponse().getStatus();
                        if (status == 200 || status == 409) {
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

        assertThat(successCount.get()).isEqualTo(2);
        assertThat(userRepository.findAll().stream()
                .filter(user -> "race-sub".equals(user.getGoogleSubject()))
                .count()).isEqualTo(1);
    }

    private JsonNode googleLogin(String idToken) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleJson(idToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private void assertUnauthorizedGoogleLogin(String idToken) throws Exception {
        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleJson(idToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Google ID token"));
    }

    private User createUser(
            String email,
            UserStatus status,
            UserType type,
            String password
    ) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(status);
        user.setType(type);
        return userRepository.saveAndFlush(user);
    }

    private EmailVerificationToken activeVerificationToken(User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(tokenHashService.sha256("verification-" + UUID.randomUUID()));
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setLastSentAt(Instant.now());
        return emailVerificationTokenRepository.saveAndFlush(token);
    }

    private GoogleIdentity identity(
            String subject,
            String email,
            boolean emailVerified,
            String name,
            String pictureUrl
    ) {
        return new GoogleIdentity(subject, email, emailVerified, name, pictureUrl);
    }

    private String googleJson(String idToken) {
        return "{\"idToken\":\"" + idToken + "\"}";
    }

    private String loginJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    @TestConfiguration
    static class GoogleAuthTestConfig {
        @Bean
        @Primary
        FakeGoogleIdentityVerifier fakeGoogleIdentityVerifier() {
            return new FakeGoogleIdentityVerifier();
        }
    }

    static class FakeGoogleIdentityVerifier implements GoogleIdentityVerifier {
        private final Map<String, GoogleIdentity> identities = new ConcurrentHashMap<>();
        private final Map<String, RuntimeException> failures = new ConcurrentHashMap<>();

        @Override
        public GoogleIdentity verify(String idToken) {
            RuntimeException failure = failures.get(idToken);
            if (failure != null) {
                throw failure;
            }
            GoogleIdentity identity = identities.get(idToken);
            if (identity == null) {
                throw new BadCredentialsException("Invalid Google ID token");
            }
            return identity;
        }

        void accept(String idToken, GoogleIdentity identity) {
            identities.put(idToken, identity);
            failures.remove(idToken);
        }

        void reject(String idToken) {
            failures.put(idToken, new BadCredentialsException("Invalid Google ID token"));
            identities.remove(idToken);
        }

        void clear() {
            identities.clear();
            failures.clear();
        }
    }
}
