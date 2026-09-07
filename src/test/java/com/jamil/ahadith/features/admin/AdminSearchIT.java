package com.jamil.ahadith.features.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.audit.entity.ActivityLog;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.user.entity.Gender;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminSearchIT extends PostgresIntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void adminUsersWithoutFiltersReturnsAllUsers() throws Exception {
        User admin = user("Admin", "admin-no-filter", UserType.admin, UserStatus.active);
        user("Member", "member-no-filter", UserType.member, UserStatus.active);
        user("Scholar", "scholar-no-filter", UserType.scholar, UserStatus.disabled);

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.pagination.totalItems").value(3));
    }

    @Test
    void adminUsersFiltersByQueryOnly() throws Exception {
        User admin = user("Admin", "admin-query", UserType.admin, UserStatus.active);
        User byName = user("Needle Name", "query-name", UserType.member, UserStatus.active);
        User byEmail = user("Other", "needle-email", UserType.scholar, UserStatus.disabled);
        user("Unmatched", "query-unmatched", UserType.member, UserStatus.active);

        JsonNode response = response(mockMvc.perform(get("/api/v1/admin/users")
                .param("q", "NEEDLE")
                .header("Authorization", bearer(admin))));

        assertThat(ids(response)).containsExactlyInAnyOrder(byName.getId().toString(), byEmail.getId().toString());
    }

    @Test
    void adminUsersFiltersByStatusOnly() throws Exception {
        User admin = user("Admin", "admin-status-filter", UserType.admin, UserStatus.active);
        User disabled = user("Disabled", "disabled-filter", UserType.member, UserStatus.disabled);
        user("Active", "active-filter", UserType.member, UserStatus.active);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("status", "disabled")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(disabled.getId().toString()))
                .andExpect(jsonPath("$.items[0].status").value("disabled"));
    }

    @Test
    void adminUsersFiltersByTypeOnly() throws Exception {
        User admin = user("Admin", "admin-type-filter", UserType.admin, UserStatus.active);
        User scholar = user("Scholar", "scholar-filter", UserType.scholar, UserStatus.active);
        user("Member", "member-filter", UserType.member, UserStatus.active);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("type", "scholar")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(scholar.getId().toString()))
                .andExpect(jsonPath("$.items[0].type").value("scholar"));
    }

    @Test
    void adminUsersCombinesAllFilters() throws Exception {
        User admin = user("Admin", "admin-all-filter", UserType.admin, UserStatus.active);
        User match = user("Combined Needle", "combined-match", UserType.scholar, UserStatus.disabled);
        user("Combined Needle", "combined-wrong-type", UserType.member, UserStatus.disabled);
        user("Combined Needle", "combined-wrong-status", UserType.scholar, UserStatus.active);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("q", "needle")
                        .param("status", "disabled")
                        .param("type", "scholar")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(match.getId().toString()));
    }

    @Test
    void adminUsersPaginatesResults() throws Exception {
        User admin = user("Admin", "admin-pagination", UserType.admin, UserStatus.active);
        for (int index = 0; index < 5; index++) {
            user("Page User " + index, "page-user-" + index, UserType.member, UserStatus.active);
        }

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("q", "Page User")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "name,asc")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(2))
                .andExpect(jsonPath("$.pagination.totalItems").value(5))
                .andExpect(jsonPath("$.pagination.totalPages").value(3));
    }

    @Test
    void adminUsersSortsByAllowedField() throws Exception {
        User admin = user("Admin", "admin-sort", UserType.admin, UserStatus.active);
        User zulu = user("Sort User Zulu", "sort-zulu", UserType.member, UserStatus.active);
        User alpha = user("Sort User Alpha", "sort-alpha", UserType.member, UserStatus.active);

        JsonNode response = response(mockMvc.perform(get("/api/v1/admin/users")
                .param("q", "Sort User")
                .param("sort", "name,asc")
                .header("Authorization", bearer(admin))));

        assertThat(ids(response)).containsExactly(alpha.getId().toString(), zulu.getId().toString());
    }

    @Test
    void adminUsersReturnsEmptyResult() throws Exception {
        User admin = user("Admin", "admin-empty", UserType.admin, UserStatus.active);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("q", "does-not-exist")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.pagination.totalItems").value(0));
    }

    @Test
    void adminUsersStillRequiresAnAdmin() throws Exception {
        User member = user("Member", "member-denied", UserType.member, UserStatus.active);

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(member)))
                .andExpect(status().isForbidden());
    }

    @Test
    void activityLogsWithoutFiltersReturnsAllLogs() throws Exception {
        User admin = user("Admin", "activity-admin-no-filter", UserType.admin, UserStatus.active);
        activityLog(admin.getId(), "users", "first message");
        activityLog(UUID.randomUUID(), "books", "second message");

        mockMvc.perform(get("/api/v1/admin/activity-logs").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.pagination.totalItems").value(2));
    }

    @Test
    void activityLogsFiltersByActorUserIdOnly() throws Exception {
        User admin = user("Admin", "activity-admin-actor", UserType.admin, UserStatus.active);
        ActivityLog match = activityLog(admin.getId(), "users", "actor match");
        activityLog(UUID.randomUUID(), "users", "other actor");

        assertSingleActivityLog(admin, "actorUserId", admin.getId().toString(), match);
    }

    @Test
    void activityLogsFiltersByTableNameOnly() throws Exception {
        User admin = user("Admin", "activity-admin-table", UserType.admin, UserStatus.active);
        ActivityLog match = activityLog(admin.getId(), "users", "table match");
        activityLog(admin.getId(), "books", "other table");

        assertSingleActivityLog(admin, "tableName", "USERS", match);
    }

    @Test
    void activityLogsFiltersByMessageOnly() throws Exception {
        User admin = user("Admin", "activity-admin-message", UserType.admin, UserStatus.active);
        ActivityLog match = activityLog(admin.getId(), "users", "Important Change Applied");
        activityLog(admin.getId(), "users", "unrelated event");

        assertSingleActivityLog(admin, "message", "CHANGE", match);
    }

    @Test
    void activityLogsCombinesAllFilters() throws Exception {
        User admin = user("Admin", "activity-admin-all", UserType.admin, UserStatus.active);
        UUID actorId = UUID.randomUUID();
        ActivityLog match = activityLog(actorId, "users", "profile updated successfully");
        activityLog(actorId, "books", "profile updated successfully");
        activityLog(UUID.randomUUID(), "users", "profile updated successfully");
        activityLog(actorId, "users", "different event");

        mockMvc.perform(get("/api/v1/admin/activity-logs")
                        .param("actorUserId", actorId.toString())
                        .param("tableName", "USERS")
                        .param("message", "UPDATED")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(match.getId().toString()));
    }

    @Test
    void activityLogsPaginatesResults() throws Exception {
        User admin = user("Admin", "activity-admin-page", UserType.admin, UserStatus.active);
        for (int index = 0; index < 5; index++) {
            ActivityLog log = activityLog(admin.getId(), "users", "page message " + index);
            setCreatedAt(log, LocalDateTime.of(2026, 1, 1, 0, index));
        }

        mockMvc.perform(get("/api/v1/admin/activity-logs")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.totalItems").value(5))
                .andExpect(jsonPath("$.pagination.totalPages").value(3));
    }

    @Test
    void activityLogsSortsByCreatedAt() throws Exception {
        User admin = user("Admin", "activity-admin-sort", UserType.admin, UserStatus.active);
        ActivityLog later = activityLog(admin.getId(), "users", "later");
        ActivityLog earlier = activityLog(admin.getId(), "users", "earlier");
        setCreatedAt(later, LocalDateTime.of(2026, 2, 2, 0, 0));
        setCreatedAt(earlier, LocalDateTime.of(2026, 1, 1, 0, 0));

        JsonNode response = response(mockMvc.perform(get("/api/v1/admin/activity-logs")
                .param("sort", "createdAt,asc")
                .header("Authorization", bearer(admin))));

        assertThat(ids(response)).containsExactly(earlier.getId().toString(), later.getId().toString());
    }

    @Test
    void activityLogsReturnsEmptyResult() throws Exception {
        User admin = user("Admin", "activity-admin-empty", UserType.admin, UserStatus.active);
        activityLog(admin.getId(), "users", "existing message");

        mockMvc.perform(get("/api/v1/admin/activity-logs")
                        .param("message", "does-not-exist")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.pagination.totalItems").value(0));
    }

    private void assertSingleActivityLog(User admin, String parameter, String value, ActivityLog expected) throws Exception {
        mockMvc.perform(get("/api/v1/admin/activity-logs")
                        .param(parameter, value)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(expected.getId().toString()));
    }

    private JsonNode response(org.springframework.test.web.servlet.ResultActions result) throws Exception {
        String body = result.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private java.util.List<String> ids(JsonNode response) {
        return response.path("items").valueStream().map(item -> item.path("id").asText()).toList();
    }

    private User user(String name, String emailPrefix, UserType type, UserStatus status) {
        User user = new User();
        user.setName(name);
        user.setEmail(emailPrefix + "@example.com");
        user.setPassword("encoded-password");
        user.setStatus(status);
        user.setType(type);
        user.setGender(Gender.male);
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        return userRepository.saveAndFlush(user);
    }

    private ActivityLog activityLog(UUID actorUserId, String tableName, String message) {
        ActivityLog log = new ActivityLog();
        log.setActorUserId(actorUserId);
        log.setActorName("Actor");
        log.setActorEmail("actor@example.com");
        log.setTableName(tableName);
        log.setMessage(message);
        return activityLogRepository.saveAndFlush(log);
    }

    private void setCreatedAt(ActivityLog log, LocalDateTime createdAt) {
        jdbc.update("update public.activity_log set created_at = ? where id = ?", createdAt, log.getId());
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateAccessToken(user);
    }
}
