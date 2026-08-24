package com.jamil.ahadith.features.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.account.service.TokenHashService;
import com.jamil.ahadith.features.audit.entity.ActivityLog;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.auth.repository.RefreshTokenSessionRepository;
import com.jamil.ahadith.features.user.entity.Gender;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserManagementIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenSessionRepository refreshTokenSessionRepository;
    @Autowired
    private ActivityLogRepository activityLogRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenHashService tokenHashService;

    @Test
    void memberAndScholarShouldNotAccessAdminUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(user("admin-deny-member", UserType.member))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(user("admin-deny-scholar", UserType.scholar))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldListSearchAndFilterUsersWithoutSensitiveFields() throws Exception {
        User admin = user("admin-list", UserType.admin);
        User activeMember = user("Alpha Search", uniqueEmail("alpha-search"), UserType.member, UserStatus.active);
        User disabledScholar = user("Beta Scholar", uniqueEmail("beta-search"), UserType.scholar, UserStatus.disabled);

        String listBody = mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.pagination.size").value(2))
                .andReturn().getResponse().getContentAsString();
        JsonNode firstItem = objectMapper.readTree(listBody).path("items").get(0);
        assertThat(firstItem.has("password")).isFalse();
        assertThat(firstItem.has("tokenVersion")).isFalse();

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("q", "alpha search")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(activeMember.getId().toString()));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("q", disabledScholar.getEmail().toUpperCase())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(disabledScholar.getId().toString()));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("status", "disabled")
                        .param("type", "scholar")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(disabledScholar.getId().toString()));
    }

    @Test
    void adminUserDetailsShouldReturnSafeDtoAndNotFoundShouldReturn404() throws Exception {
        User admin = user("admin-detail", UserType.admin);
        User target = user("Detail Target", uniqueEmail("detail-target"), UserType.member, UserStatus.active);

        String body = mockMvc.perform(get("/api/v1/admin/users/{id}", target.getId())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(target.getId().toString()))
                .andExpect(jsonPath("$.email").value(target.getEmail()))
                .andReturn().getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(body);
        assertThat(response.has("password")).isFalse();
        assertThat(response.has("tokenVersion")).isFalse();

        mockMvc.perform(get("/api/v1/admin/users/{id}", UUID.randomUUID())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminStatusChangesShouldInvalidateTokensKeepRevokedSessionsAndAudit() throws Exception {
        User admin = user("admin-status", UserType.admin);
        User target = user("Status Target", uniqueEmail("status-target"), UserType.member, UserStatus.active);
        JsonNode session = login(target.getEmail(), "12345678");
        String oldAccessToken = session.get("accessToken").asText();
        String oldRefreshToken = session.get("refreshToken").asText();

        mockMvc.perform(put("/api/v1/admin/users/{id}/status", target.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"disabled\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("disabled"));

        assertThat(userRepository.findById(target.getId()).orElseThrow().getStatus())
                .isEqualTo(UserStatus.disabled);
        assertThat(refreshTokenSessionRepository.findByTokenHash(tokenHashService.sha256(oldRefreshToken)).orElseThrow().getRevokedAt())
                .isNotNull();
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/admin/users/{id}/status", target.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"active\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("active"));
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());

        assertThat(activityLogExists(target.getId(), "admin user status changed")).isTrue();
        long auditCount = activityLogCount(target.getId(), "admin user status changed");
        mockMvc.perform(put("/api/v1/admin/users/{id}/status", target.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"active\"}"))
                .andExpect(status().isOk());
        assertThat(activityLogCount(target.getId(), "admin user status changed")).isEqualTo(auditCount);
    }

    @Test
    void adminShouldNotDisableSelfOrChangeOwnType() throws Exception {
        User admin = user("admin-self", UserType.admin);

        mockMvc.perform(put("/api/v1/admin/users/{id}/status", admin.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"disabled\"}"))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findById(admin.getId()).orElseThrow().getStatus()).isEqualTo(UserStatus.active);

        mockMvc.perform(put("/api/v1/admin/users/{id}/type", admin.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"member\"}"))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findById(admin.getId()).orElseThrow().getType()).isEqualTo(UserType.admin);
    }

    @Test
    void adminTypeChangesShouldWorkInvalidateSessionsAndAudit() throws Exception {
        User admin = user("admin-type", UserType.admin);
        User target = user("Type Target", uniqueEmail("type-target"), UserType.member, UserStatus.active);
        JsonNode session = login(target.getEmail(), "12345678");
        String oldAccessToken = session.get("accessToken").asText();
        String oldRefreshToken = session.get("refreshToken").asText();

        mockMvc.perform(put("/api/v1/admin/users/{id}/type", target.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"scholar\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("scholar"));

        mockMvc.perform(put("/api/v1/admin/users/{id}/type", target.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("admin"));

        assertThat(userRepository.findById(target.getId()).orElseThrow().getType()).isEqualTo(UserType.admin);
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
        assertThat(activityLogExists(target.getId(), "admin user type changed")).isTrue();
    }

    @Test
    void memberScholarAndAdminShouldUpdateOwnProfileOnly() throws Exception {
        assertProfileUpdateWorks(user("profile-member", UserType.member));
        assertProfileUpdateWorks(user("profile-scholar", UserType.scholar));
        assertProfileUpdateWorks(user("profile-admin", UserType.admin));
    }

    @Test
    void profileUpdateShouldRejectInvalidInputAndIgnoreProtectedFields() throws Exception {
        User user = user("Protected User", uniqueEmail("profile-protected"), UserType.member, UserStatus.active);
        User other = user("Other User", uniqueEmail("profile-other"), UserType.member, UserStatus.active);

        mockMvc.perform(put("/api/v1/me")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"gender\":\"male\",\"birthDate\":\"2000-01-01\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/me")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Valid Name\",\"gender\":\"unknown\",\"birthDate\":\"2000-01-01\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/me")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Valid Name\",\"gender\":\"male\",\"birthDate\":\"2999-01-01\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/me")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Valid Name\",\"gender\":\"male\",\"birthDate\":\"2020-01-01\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/me")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Profile Updated  ",
                                  "gender": "female",
                                  "birthDate": "1990-05-20",
                                  "email": "changed@example.com",
                                  "type": "admin",
                                  "status": "disabled",
                                  "tokenVersion": 99
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Profile Updated"))
                .andExpect(jsonPath("$.gender").value("female"))
                .andExpect(jsonPath("$.birthDate").value("1990-05-20"));

        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        assertThat(refreshed.getEmail()).isEqualTo(user.getEmail());
        assertThat(refreshed.getType()).isEqualTo(UserType.member);
        assertThat(refreshed.getStatus()).isEqualTo(UserStatus.active);
        assertThat(refreshed.getTokenVersion()).isZero();
        assertThat(userRepository.findById(other.getId()).orElseThrow().getName()).isEqualTo("Other User");
    }

    @Test
    void changePasswordShouldValidateRotateCredentialsInvalidateTokensAndKeepAuditSafe() throws Exception {
        User user = user("Password User", uniqueEmail("password-user"), UserType.member, UserStatus.active);

        mockMvc.perform(put("/api/v1/me/password")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong-password\",\"newPassword\":\"new-password-1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));

        mockMvc.perform(put("/api/v1/me/password")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"12345678\",\"newPassword\":\"short\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/me/password")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"12345678\",\"newPassword\":\"12345678\"}"))
                .andExpect(status().isBadRequest());

        JsonNode session = login(user.getEmail(), "12345678");
        String oldAccessToken = session.get("accessToken").asText();
        String oldRefreshToken = session.get("refreshToken").asText();

        mockMvc.perform(put("/api/v1/me/password")
                        .header("Authorization", "Bearer " + oldAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"12345678\",\"newPassword\":\"new-password-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(user.getEmail(), "12345678")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(user.getEmail(), "new-password-1")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());

        ActivityLog log = activityLogRepository.findAll().stream()
                .filter(item -> user.getId().equals(item.getRecordId()))
                .filter(item -> item.getMessage() != null && item.getMessage().contains("password changed"))
                .findFirst()
                .orElseThrow();
        String auditPayload = objectMapper.writeValueAsString(log);
        assertThat(auditPayload).doesNotContain("12345678", "new-password-1", user.getPassword(), "passwordHash");
    }

    private void assertProfileUpdateWorks(User user) throws Exception {
        mockMvc.perform(put("/api/v1/me")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  Updated " + user.getType() + "  \",\"gender\":\"female\",\"birthDate\":\"1992-02-02\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated " + user.getType()))
                .andExpect(jsonPath("$.gender").value("female"))
                .andExpect(jsonPath("$.birthDate").value("1992-02-02"));

        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        assertThat(refreshed.getName()).isEqualTo("Updated " + user.getType());
        assertThat(refreshed.getGender()).isEqualTo(Gender.female);
        assertThat(refreshed.getBirthDate()).isEqualTo(LocalDate.of(1992, 2, 2));
        assertThat(activityLogExists(user.getId(), "profile updated")).isTrue();
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateAccessToken(user);
    }

    private JsonNode login(String email, String password) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private String loginJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private User user(String prefix, UserType type) {
        return user("Test User", uniqueEmail(prefix), type, UserStatus.active);
    }

    private User user(String name, String email, UserType type, UserStatus status) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setStatus(status);
        user.setType(type);
        user.setGender(Gender.male);
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        return userRepository.saveAndFlush(user);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }

    private boolean activityLogExists(UUID recordId, String message) {
        return activityLogCount(recordId, message) > 0;
    }

    private long activityLogCount(UUID recordId, String message) {
        return activityLogRepository.findAll().stream()
                .filter(log -> recordId.equals(log.getRecordId()))
                .filter(log -> log.getMessage() != null && log.getMessage().contains(message))
                .count();
    }
}
