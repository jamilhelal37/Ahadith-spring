package com.jamil.ahadith.features.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.core.mail.TestEmailService;
import com.jamil.ahadith.features.account.repository.EmailVerificationTokenRepository;
import com.jamil.ahadith.features.account.repository.PasswordResetTokenRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountRefactorSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private TestEmailService testEmailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void verificationToken_ShouldExpire_WhenExpiresAtEqualsNow() throws Exception {
        String email = "expire-now@example.com";
        createUser(email, UserStatus.pending_confirmation);
        
        // Manual creation to control expiresAt precisely
        mockMvc.perform(post("/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk());
        
        String token = testEmailService.verificationTokenFor(email);
        var dbToken = emailVerificationTokenRepository.findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(
                userRepository.findByEmail(email).get()).getFirst();
        
        Instant now = Instant.now();
        dbToken.setExpiresAt(now);
        emailVerificationTokenRepository.saveAndFlush(dbToken);
        
        // We need to make sure the service uses the SAME 'now' or at least a 'now' that is >= our 'now'
        // Since we can't easily inject 'now' into the service, we rely on the fact that 
        // Instant.now() in the service will be >= dbToken.getExpiresAt()
        
        mockMvc.perform(post("/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void concurrentEmailVerification_OnlyOneShouldSucceed() throws Exception {
        String email = "concurrent-verify@example.com";
        createUser(email, UserStatus.pending_confirmation);
        
        mockMvc.perform(post("/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk());
        
        String token = testEmailService.verificationTokenFor(email);
        
        int threadCount = 2;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        var result = mockMvc.perform(post("/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"token\":\"" + token + "\"}"))
                                .andReturn().getResponse();
                        
                        if (result.getStatus() == 200) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);
    }

    @Test
    void passwordResetToken_ShouldExpire_WhenExpiresAtEqualsNow() throws Exception {
        String email = "reset-expire-now@example.com";
        createUser(email, UserStatus.active);
        
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk());
        
        String token = testEmailService.passwordResetTokenFor(email);
        var dbToken = passwordResetTokenRepository.findByUserAndConsumedAtIsNullOrderByCreatedAtDesc(
                userRepository.findByEmail(email).get()).getFirst();
        
        dbToken.setExpiresAt(Instant.now());
        passwordResetTokenRepository.saveAndFlush(dbToken);
        
        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\", \"newPassword\":\"newPassword123\"}"))
                .andExpect(status().isUnauthorized());
    }

    private User createUser(String email, UserStatus status) {
        User user = new User();
        user.setName("Integration Test User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setStatus(status);
        user.setType(UserType.member);
        user.setTokenVersion(0);
        return userRepository.saveAndFlush(user);
    }
}
