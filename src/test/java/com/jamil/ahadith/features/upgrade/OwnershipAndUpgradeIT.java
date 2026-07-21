package com.jamil.ahadith.features.upgrade;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;

import com.jamil.ahadith.features.interaction.entity.Question;

import com.jamil.ahadith.features.notification.entity.Notification;

import com.jamil.ahadith.features.interaction.entity.Comment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.features.interaction.entity.Favorite;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import com.jamil.ahadith.features.search.entity.SearchHistory;
import com.jamil.ahadith.features.search.entity.SearchSource;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.interaction.repository.FavoriteRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.notification.repository.NotificationRepository;
import com.jamil.ahadith.features.search.repository.SearchHistoryRepository;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.core.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class OwnershipAndUpgradeIT extends PostgresIntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HadithRepository hadithRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    @Autowired
    private ActivityLogRepository activityLogRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Test
    void memberQuestionOwnershipShouldBeScopedToCurrentUser() throws Exception {
        User owner = user("question-owner@example.com", UserType.member);
        User other = user("question-other@example.com", UserType.member);

        JsonNode created = objectMapper.readTree(mockMvc.perform(post("/me/questions")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"askerText\":\"private question\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        UUID questionId = UUID.fromString(created.get("id").asText());
        mockMvc.perform(get("/me/questions/" + questionId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/me/questions/" + questionId)
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"askerText\":\"tamper\"}"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/me/questions/" + questionId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
    }

    @Test
    void memberCommentOwnershipShouldBeScopedToCurrentUser() throws Exception {
        User owner = user("comment-owner@example.com", UserType.member);
        User other = user("comment-other@example.com", UserType.member);
        Hadith hadith = hadith();

        JsonNode created = objectMapper.readTree(mockMvc.perform(post("/me/hadiths/" + hadith.getId() + "/comments")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"private comment\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        UUID commentId = UUID.fromString(created.get("id").asText());
        mockMvc.perform(get("/me/comments/" + commentId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/me/comments/" + commentId)
                        .header("Authorization", bearer(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"tamper\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/me/comments/" + commentId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
    }

    @Test
    void favoritesShouldBeUniqueAndDeleteByHadithShouldBeOwnerScoped() throws Exception {
        User owner = user("favorite-owner@example.com", UserType.member);
        User other = user("favorite-other@example.com", UserType.member);
        Hadith hadith = hadith();

        mockMvc.perform(post("/me/favorites/" + hadith.getId()).header("Authorization", bearer(owner)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/me/favorites/" + hadith.getId()).header("Authorization", bearer(owner)))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/me/favorites/" + hadith.getId()).header("Authorization", bearer(other)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/me/favorites/" + UUID.randomUUID()).header("Authorization", bearer(owner)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/me/favorites/" + hadith.getId()).header("Authorization", bearer(other)))
                .andExpect(status().isNoContent());
        assertThat(favoriteRepository.findByUserIdAndHadithId(owner.getId(), hadith.getId())).isPresent();
        assertThat(favoriteRepository.findByUserIdAndHadithId(other.getId(), hadith.getId())).isEmpty();

        mockMvc.perform(delete("/me/favorites/" + hadith.getId()).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        assertThat(favoriteRepository.findByUserIdAndHadithId(owner.getId(), hadith.getId())).isPresent();

        Favorite favorite = favoriteRepository.findByUserIdAndHadithId(owner.getId(), hadith.getId()).orElseThrow();
        assertThat(favorite.getUser().getId()).isEqualTo(owner.getId());

        mockMvc.perform(delete("/me/favorites/" + hadith.getId()).header("Authorization", bearer(owner)))
                .andExpect(status().isNoContent());
        assertThat(favoriteRepository.findByUserIdAndHadithId(owner.getId(), hadith.getId())).isEmpty();
    }

    @Test
    void favoriteEndpointsShouldRequireAuthentication() throws Exception {
        UUID hadithId = hadith().getId();

        mockMvc.perform(get("/me/favorites"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/me/favorites/" + hadithId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/me/favorites/" + hadithId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void currentUserFavoritesShouldReturnHadithCardsOnlyWithPaginationAndStableFavoriteOrder() throws Exception {
        User owner = user("favorites-list-owner@example.com", UserType.member);
        User other = user("favorites-list-other@example.com", UserType.member);
        UUID muhaddithId = uuid(201);
        UUID rawiId = uuid(202);
        UUID rulingId = uuid(203);
        UUID bookId = uuid(204);
        UUID explanationId = uuid(205);
        UUID topicId = uuid(206);
        UUID subValidId = uuid(207);
        UUID firstId = uuid(208);
        UUID secondId = uuid(209);
        UUID otherUserHadithId = uuid(210);

        jdbc.update("insert into public.muhaddiths (id, name, gender, about) values (?, ?, cast(? as public.gender), ?)",
                muhaddithId, "Favorite Muhaddith", "male", "Favorite muhaddith about");
        jdbc.update("insert into public.rawis (id, name, gender, about) values (?, ?, cast(? as public.gender), ?)",
                rawiId, "Favorite Rawi", "male", "Favorite rawi about");
        jdbc.update("insert into public.ruling (id, name) values (?, ?)", rulingId, "Favorite Ruling");
        jdbc.update("insert into public.books (id, name, muhaddith) values (?, ?, ?)",
                bookId, "Favorite Book", muhaddithId);
        jdbc.update("insert into public.explaining (id, text) values (?, ?)",
                explanationId, "Explanation text");
        jdbc.update("insert into public.topics (id, name) values (?, ?)", topicId, "Favorite Topic");
        insertHadith(subValidId, "sub valid text", 99, bookId, null, null, null, null, null);
        insertHadith(firstId, "first text", 1, bookId, rawiId, rulingId, explanationId, "first sanad", subValidId);
        insertHadith(secondId, "second text", 2, bookId, null, null, null, null, null);
        insertHadith(otherUserHadithId, "other user text", 3, bookId, null, null, null, null, null);
        jdbc.update("insert into public.topic_classes (id, topic, hadith) values (?, ?, ?)", uuid(211), topicId, firstId);
        insertFavorite(uuid(301), owner.getId(), firstId, "2026-01-01 10:00:00+00");
        insertFavorite(uuid(302), owner.getId(), secondId, "2026-01-02 10:00:00+00");
        insertFavorite(uuid(303), other.getId(), otherUserHadithId, "2026-01-03 10:00:00+00");

        String response = mockMvc.perform(get("/me/favorites")
                        .header("Authorization", bearer(owner))
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(secondId.toString()))
                .andExpect(jsonPath("$.items[0].text").value("second text"))
                .andExpect(jsonPath("$.items[0].normalText").value("second text"))
                .andExpect(jsonPath("$.items[0].sanad", nullValue()))
                .andExpect(jsonPath("$.items[0].book.id").value(bookId.toString()))
                .andExpect(jsonPath("$.items[0].book.name").value("Favorite Book"))
                .andExpect(jsonPath("$.items[0].muhaddith.id").value(muhaddithId.toString()))
                .andExpect(jsonPath("$.items[0].topics", empty()))
                .andExpect(jsonPath("$.items[0].hasExplanation").value(false))
                .andExpect(jsonPath("$.items[0].hasSubValid").value(false))
                .andExpect(jsonPath("$.items[0].favoriteId").doesNotExist())
                .andExpect(jsonPath("$.items[0].user").doesNotExist())
                .andExpect(jsonPath("$.items[0].userId").doesNotExist())
                .andExpect(jsonPath("$.items[0].email").doesNotExist())
                .andExpect(jsonPath("$.items[0].searchText").doesNotExist())
                .andExpect(jsonPath("$.items[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.items[0].updatedAt").doesNotExist())
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(1))
                .andExpect(jsonPath("$.pagination.totalItems").value(2))
                .andExpect(jsonPath("$.pagination.totalPages").value(2))
                .andExpect(jsonPath("$.pagination.hasNext").value(true))
                .andExpect(jsonPath("$.pagination.hasPrevious").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode firstItem = objectMapper.readTree(response).get("items").get(0);
        assertThat(firstItem.size()).isEqualTo(13);

        mockMvc.perform(get("/me/favorites")
                        .header("Authorization", bearer(owner))
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(firstId.toString()))
                .andExpect(jsonPath("$.items[0].sanad").value("first sanad"))
                .andExpect(jsonPath("$.items[0].rawi.id").value(rawiId.toString()))
                .andExpect(jsonPath("$.items[0].ruling.id").value(rulingId.toString()))
                .andExpect(jsonPath("$.items[0].topics[*].id", contains(topicId.toString())))
                .andExpect(jsonPath("$.items[0].hasExplanation").value(true))
                .andExpect(jsonPath("$.items[0].hasSubValid").value(true))
                .andExpect(jsonPath("$.items[0].subValid").doesNotExist())
                .andExpect(jsonPath("$.items[0].subValidId").doesNotExist())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.hasNext").value(false))
                .andExpect(jsonPath("$.pagination.hasPrevious").value(true));

        mockMvc.perform(get("/me/favorites")
                        .header("Authorization", bearer(other))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(otherUserHadithId.toString()))
                .andExpect(jsonPath("$.pagination.totalItems").value(1));

        mockMvc.perform(get("/me/favorites")
                        .header("Authorization", bearer(owner))
                        .param("page", "2")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", empty()))
                .andExpect(jsonPath("$.pagination.totalItems").value(2));

        mockMvc.perform(get("/me/favorites")
                        .header("Authorization", bearer(owner))
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/me/favorites")
                        .header("Authorization", bearer(owner))
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/me/favorites")
                        .header("Authorization", bearer(owner))
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.size").value(50));

        User emptyUser = user("favorites-empty@example.com", UserType.member);
        mockMvc.perform(get("/me/favorites").header("Authorization", bearer(emptyUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", empty()))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(20))
                .andExpect(jsonPath("$.pagination.totalItems").value(0))
                .andExpect(jsonPath("$.pagination.totalPages").value(0))
                .andExpect(jsonPath("$.pagination.hasNext").value(false))
                .andExpect(jsonPath("$.pagination.hasPrevious").value(false));

        User admin = user("favorites-admin@example.com", UserType.admin);
        mockMvc.perform(get("/admin/favorites").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].user").exists())
                .andExpect(jsonPath("$.items[0].hadith").exists())
                .andExpect(jsonPath("$.items[0].createdAt").exists());
    }

    @Test
    void searchHistoryDeletionShouldIncludeOwnership() throws Exception {
        User owner = user("history-owner@example.com", UserType.member);
        User other = user("history-other@example.com", UserType.member);
        SearchHistory otherHistory = new SearchHistory();
        otherHistory.setUser(other);
        otherHistory.setSearchText("private history");
        otherHistory.setSearchSource(SearchSource.Hadith);
        otherHistory = searchHistoryRepository.saveAndFlush(otherHistory);

        mockMvc.perform(delete("/me/search-history/" + otherHistory.getId()).header("Authorization", bearer(owner)))
                .andExpect(status().isNoContent());

        assertThat(searchHistoryRepository.findById(otherHistory.getId())).isPresent();
    }

    @Test
    void upgradeRequestShouldRejectDuplicatesAndApproveOrRejectTransactionally() throws Exception {
        User member = user("upgrade-member@example.com", UserType.member);
        User rejectedMember = user("upgrade-reject@example.com", UserType.member);
        User admin = user("upgrade-admin@example.com", UserType.admin);

        JsonNode created = objectMapper.readTree(mockMvc.perform(post("/me/upgrade-requests")
                        .header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/me/upgrade-requests")
                        .header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());

        UUID requestId = UUID.fromString(created.get("id").asText());
        mockMvc.perform(patch("/admin/upgrade-requests/" + requestId + "/review")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVE\",\"reviewNotes\":\"ok\"}"))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(member.getId()).orElseThrow().getType()).isEqualTo(UserType.scholar);
        assertThat(activityLogRepository.existsByTableName("upgrade_requests")).isTrue();
        assertThat(notificationRepository.findAll()).anyMatch(notification ->
                notification.getUser() != null && member.getId().equals(notification.getUser().getId()));

        JsonNode rejected = objectMapper.readTree(mockMvc.perform(post("/me/upgrade-requests")
                        .header("Authorization", bearer(rejectedMember))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(patch("/admin/upgrade-requests/" + rejected.get("id").asText() + "/review")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECT\",\"rejectionReason\":\"missing documents\"}"))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(rejectedMember.getId()).orElseThrow().getType()).isEqualTo(UserType.member);
    }

    private User user(String email, UserType type) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setStatus(UserStatus.active);
        user.setType(type);
        return userRepository.saveAndFlush(user);
    }

    private Hadith hadith() {
        Hadith hadith = new Hadith();
        hadith.setText("Hadith " + UUID.randomUUID());
        hadith.setSearchText(hadith.getText());
        hadith.setNormalText(hadith.getText());
        hadith.setHadithNumber(Math.abs(UUID.randomUUID().hashCode()));
        hadith.setType(HadithType.marfu);
        return hadithRepository.saveAndFlush(hadith);
    }

    private void insertHadith(UUID id, String text, int number, UUID bookId, UUID rawiId,
                              UUID rulingId, UUID explanationId, String sanad, UUID subValidId) {
        jdbc.update("""
                insert into public.ahadith
                    (id, text, hadith_number, type, book, rawi, ruling, explaining, sanad, sub_valid)
                values (?, ?, ?, cast(? as public.hadith_type), ?, ?, ?, ?, ?, ?)
                """, id, text, number, "marfu", bookId, rawiId, rulingId,
                explanationId, sanad, subValidId);
    }

    private void insertFavorite(UUID id, UUID userId, UUID hadithId, String createdAt) {
        jdbc.update("""
                insert into public.favorites (id, user_id, hadith, created_at)
                values (?, ?, ?, cast(? as timestamptz))
                """, id, userId, hadithId, createdAt);
    }

    private UUID uuid(int value) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(value));
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateAccessToken(user);
    }
}
