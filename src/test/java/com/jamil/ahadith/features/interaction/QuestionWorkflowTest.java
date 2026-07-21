package com.jamil.ahadith.features.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuestionWorkflowTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("delete from \"activity_log\"");
        jdbcTemplate.execute("delete from \"questions\"");
        jdbcTemplate.execute("delete from \"ahadith\"");
        jdbcTemplate.execute("delete from \"books\"");
        jdbcTemplate.execute("delete from \"rawis\"");
        jdbcTemplate.execute("delete from \"muhaddiths\"");
        jdbcTemplate.execute("delete from \"ruling\"");
        jdbcTemplate.execute("delete from \"users\"");
    }

    @Test
    void memberShouldCreateQuestionsListOnlyOwnQuestionsAndCannotUpdate() throws Exception {
        User owner = user("questions-owner@example.com", UserType.member);
        User other = user("questions-other@example.com", UserType.member);
        UUID hadithId = seedHadith("created with hadith", "created normal", 10);

        mockMvc.perform(post("/me/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"askerText\":\"anonymous attempt\"}"))
                .andExpect(status().isUnauthorized());

        JsonNode first = readJson(mockMvc.perform(post("/me/questions")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"askerText\":\"first owner question\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hadith", nullValue()))
                .andExpect(jsonPath("$.answerText", nullValue()))
                .andExpect(jsonPath("$.isActive").value(false)));

        JsonNode second = readJson(mockMvc.perform(post("/me/questions")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hadithId":"%s","askerText":"second owner question"}
                                """.formatted(hadithId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hadith.id").value(hadithId.toString()))
                .andExpect(jsonPath("$.hadith.text").value("created with hadith"))
                .andExpect(jsonPath("$.hadith.normalText").value("created normal"))
                .andExpect(jsonPath("$.hadith.sanad").value("test sanad")));

        JsonNode otherQuestion = readJson(mockMvc.perform(post("/me/questions")
                        .header("Authorization", bearer(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"askerText\":\"other question\"}"))
                .andExpect(status().isCreated()));

        UUID firstId = UUID.fromString(first.get("id").asText());
        UUID secondId = UUID.fromString(second.get("id").asText());
        UUID otherQuestionId = UUID.fromString(otherQuestion.get("id").asText());
        setQuestionCreatedAt(firstId, "2026-01-01 10:00:00");
        setQuestionCreatedAt(secondId, "2026-01-02 10:00:00");
        setQuestionCreatedAt(otherQuestionId, "2026-01-03 10:00:00");

        mockMvc.perform(get("/me/questions")
                        .header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(secondId.toString()))
                .andExpect(jsonPath("$[1].id").value(firstId.toString()));

        mockMvc.perform(get("/me/questions/{id}", firstId)
                        .header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/me/questions/{id}", firstId)
                        .header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());

        assertQuestionExists(firstId);

        mockMvc.perform(put("/me/questions/{id}", secondId)
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"askerText\":\"tamper\"}"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(patch("/me/questions/{id}", secondId)
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"askerText\":\"tamper\"}"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(post("/me/questions")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hadithId":"%s","askerText":"missing hadith question"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/me/questions/{id}", firstId)
                        .header("Authorization", bearer(owner)))
                .andExpect(status().isNoContent());

        assertQuestionDeleted(firstId);
    }

    @Test
    void scholarShouldDraftActivateHideAndEditAnswersWithoutChangingStatus() throws Exception {
        User member = user("questions-member@example.com", UserType.member);
        User scholar = user("questions-scholar@example.com", UserType.scholar);
        User otherMember = user("questions-other-member@example.com", UserType.member);
        UUID hadithId = seedHadith("question hadith text", "question hadith normal", 1);

        UUID questionId = createQuestion(member, hadithId, "needs answer");
        UUID otherQuestionId = createQuestion(otherMember, null, "other member question");
        setQuestionCreatedAt(questionId, "2026-01-01 10:00:00");
        setQuestionCreatedAt(otherQuestionId, "2026-01-02 10:00:00");

        mockMvc.perform(get("/scholar/questions")
                        .header("Authorization", bearer(member)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/scholar/questions")
                        .header("Authorization", bearer(scholar))
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id").value(otherQuestionId.toString()))
                .andExpect(jsonPath("$.items[1].id").value(questionId.toString()))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(20))
                .andExpect(jsonPath("$.pagination.totalItems").value(2))
                .andExpect(jsonPath("$.pagination.totalPages").value(1))
                .andExpect(jsonPath("$.pagination.hasNext").value(false))
                .andExpect(jsonPath("$.pagination.hasPrevious").value(false));

        mockMvc.perform(patch("/scholar/questions/{id}/status", questionId)
                        .header("Authorization", bearer(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":true}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/scholar/questions/{id}/answer", questionId)
                        .header("Authorization", bearer(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"draft answer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText").value("draft answer"))
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.updatedBy.id").value(scholar.getId().toString()));

        mockMvc.perform(get("/me/questions/{id}", questionId)
                        .header("Authorization", bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText", nullValue()))
                .andExpect(jsonPath("$.isActive").value(false));

        mockMvc.perform(get("/scholar/questions/{id}", questionId)
                        .header("Authorization", bearer(scholar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText").value("draft answer"))
                .andExpect(jsonPath("$.hadith.id").value(hadithId.toString()))
                .andExpect(jsonPath("$.hadith.searchText").doesNotExist())
                .andExpect(jsonPath("$.hadith.createdAt").doesNotExist())
                .andExpect(jsonPath("$.hadith.questions").doesNotExist())
                .andExpect(jsonPath("$.asker.password").doesNotExist());

        mockMvc.perform(patch("/scholar/questions/{id}/status", questionId)
                        .header("Authorization", bearer(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        mockMvc.perform(get("/me/questions/{id}", questionId)
                        .header("Authorization", bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText").value("draft answer"))
                .andExpect(jsonPath("$.isActive").value(true));

        mockMvc.perform(patch("/scholar/questions/{id}/status", questionId)
                        .header("Authorization", bearer(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText").value("draft answer"))
                .andExpect(jsonPath("$.isActive").value(false));

        mockMvc.perform(get("/me/questions/{id}", questionId)
                        .header("Authorization", bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText", nullValue()));

        mockMvc.perform(patch("/scholar/questions/{id}/status", questionId)
                        .header("Authorization", bearer(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/scholar/questions/{id}/answer", questionId)
                        .header("Authorization", bearer(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"edited active answer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText").value("edited active answer"))
                .andExpect(jsonPath("$.isActive").value(true));

        mockMvc.perform(get("/me/questions/{id}", questionId)
                        .header("Authorization", bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText").value("edited active answer"));

        mockMvc.perform(delete("/me/questions/{id}", questionId)
                        .header("Authorization", bearer(member)))
                .andExpect(status().isNoContent());

        assertQuestionDeleted(questionId);
    }

    @Test
    void questionEndpointsShouldRequireExpectedAuthentication() throws Exception {
        UUID questionId = UUID.randomUUID();

        mockMvc.perform(get("/me/questions"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/me/questions/{id}", questionId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/me/questions/{id}", questionId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/scholar/questions/{id}/answer", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"answer\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void memberResponseShouldExposeNoEntityOrDraftFields() throws Exception {
        User member = user("questions-shape-member@example.com", UserType.member);
        User scholar = user("questions-shape-scholar@example.com", UserType.scholar);
        UUID hadithId = seedHadith("shape text", "shape normal", 5);
        UUID questionId = createQuestion(member, hadithId, "shape question");

        mockMvc.perform(patch("/scholar/questions/{id}/answer", questionId)
                        .header("Authorization", bearer(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"hidden draft\"}"))
                .andExpect(status().isOk());

        String response = mockMvc.perform(get("/me/questions/{id}", questionId)
                        .header("Authorization", bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questionId.toString()))
                .andExpect(jsonPath("$.askerText").value("shape question"))
                .andExpect(jsonPath("$.answerText", nullValue()))
                .andExpect(jsonPath("$.asker").doesNotExist())
                .andExpect(jsonPath("$.updatedBy").doesNotExist())
                .andExpect(jsonPath("$.hadith.id").value(hadithId.toString()))
                .andExpect(jsonPath("$.hadith.searchText").doesNotExist())
                .andExpect(jsonPath("$.hadith.createdBy").doesNotExist())
                .andExpect(jsonPath("$.hadith.updatedBy").doesNotExist())
                .andExpect(jsonPath("$.hadith.favorites").doesNotExist())
                .andExpect(jsonPath("$.hadith.comments").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.size()).isEqualTo(7);
        assertThat(root.get("hadith").size()).isEqualTo(10);

        mockMvc.perform(get("/me/questions")
                        .header("Authorization", bearer(user("questions-empty@example.com", UserType.member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    private UUID createQuestion(User user, UUID hadithId, String askerText) throws Exception {
        String hadithPart = hadithId == null ? "" : "\"hadithId\":\"" + hadithId + "\",";
        JsonNode created = readJson(mockMvc.perform(post("/me/questions")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + hadithPart + "\"askerText\":\"" + askerText + "\"}"))
                .andExpect(status().isCreated()));
        return UUID.fromString(created.get("id").asText());
    }

    private JsonNode readJson(org.springframework.test.web.servlet.ResultActions actions) throws Exception {
        return objectMapper.readTree(actions.andReturn().getResponse().getContentAsString());
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

    private UUID seedHadith(String text, String normalText, int number) {
        UUID hadithId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into "ahadith" ("id", "text", "normal_text", "search_text", "hadith_number", "type", "sanad")
                values (?, ?, ?, ?, ?, ?, ?)
                """, hadithId, text, normalText, "internal search text", number, "marfu", "test sanad");
        return hadithId;
    }

    private void setQuestionCreatedAt(UUID questionId, String createdAt) {
        jdbcTemplate.update("update \"questions\" set \"created_at\" = ? where \"id\" = ?", createdAt, questionId);
    }

    private void assertQuestionExists(UUID questionId) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from \"questions\" where \"id\" = ?",
                Integer.class, questionId);
        assertThat(count).isEqualTo(1);
    }

    private void assertQuestionDeleted(UUID questionId) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from \"questions\" where \"id\" = ?",
                Integer.class, questionId);
        assertThat(count).isZero();
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateAccessToken(user);
    }
}
