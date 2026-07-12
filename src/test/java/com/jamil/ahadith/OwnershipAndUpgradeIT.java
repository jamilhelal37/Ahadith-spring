package com.jamil.ahadith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.entities.Favorite;
import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.HadithType;
import com.jamil.ahadith.entities.SearchHistory;
import com.jamil.ahadith.entities.SearchSource;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.entities.UserStatus;
import com.jamil.ahadith.entities.UserType;
import com.jamil.ahadith.repositories.ActivityLogRepository;
import com.jamil.ahadith.repositories.FavoriteRepository;
import com.jamil.ahadith.repositories.HadithRepository;
import com.jamil.ahadith.repositories.NotificationRepository;
import com.jamil.ahadith.repositories.SearchHistoryRepository;
import com.jamil.ahadith.repositories.UserRepository;
import com.jamil.ahadith.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                        .header("Authorization", bearer(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"askerText\":\"tamper\"}"))
                .andExpect(status().isNotFound());
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

        mockMvc.perform(delete("/me/favorites/" + hadith.getId()).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        assertThat(favoriteRepository.findByUserIdAndHadithId(owner.getId(), hadith.getId())).isPresent();

        Favorite favorite = favoriteRepository.findByUserIdAndHadithId(owner.getId(), hadith.getId()).orElseThrow();
        assertThat(favorite.getUser().getId()).isEqualTo(owner.getId());
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

    private String bearer(User user) {
        return "Bearer " + jwtService.generateAccessToken(user);
    }
}
