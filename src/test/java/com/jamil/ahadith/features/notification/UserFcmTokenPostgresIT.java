package com.jamil.ahadith.features.notification;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.features.notification.service.UserFcmTokenService;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserFcmTokenPostgresIT extends PostgresIntegrationTestBase {
    @Autowired
    private UserFcmTokenService userFcmTokenService;
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerCurrentUserTokenShouldNotDuplicateSameUserTokenAndShouldTransferExistingToken() {
        User firstUser = user("first-fcm@example.com");
        User secondUser = user("second-fcm@example.com");

        authenticate(firstUser);
        userFcmTokenService.registerCurrentUserToken(" shared-token ");
        userFcmTokenService.registerCurrentUserToken("shared-token");

        assertThat(tokenCount("shared-token")).isEqualTo(1);
        assertThat(tokenOwner("shared-token")).isEqualTo(firstUser.getId());

        authenticate(secondUser);
        userFcmTokenService.registerCurrentUserToken("shared-token");

        assertThat(tokenCount("shared-token")).isEqualTo(1);
        assertThat(tokenOwner("shared-token")).isEqualTo(secondUser.getId());
    }

    private User user(String email) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setStatus(UserStatus.active);
        user.setType(UserType.member);
        return userRepository.saveAndFlush(user);
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user.getEmail(), null, List.of())
        );
    }

    private long tokenCount(String token) {
        return jdbc.queryForObject(
                "select count(*) from public.user_fcm_tokens where fcm_token = ?",
                Long.class,
                token
        );
    }

    private UUID tokenOwner(String token) {
        return jdbc.queryForObject(
                "select user_id from public.user_fcm_tokens where fcm_token = ?",
                UUID.class,
                token
        );
    }
}
