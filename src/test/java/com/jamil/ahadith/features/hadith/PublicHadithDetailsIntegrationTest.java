package com.jamil.ahadith.features.hadith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicHadithDetailsIntegrationTest {
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

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("delete from \"favorites\"");
        jdbcTemplate.execute("delete from \"comments\"");
        jdbcTemplate.execute("delete from \"topic_classes\"");
        jdbcTemplate.execute("delete from \"ahadith\"");
        jdbcTemplate.execute("delete from \"books\"");
        jdbcTemplate.execute("delete from \"explaining\"");
        jdbcTemplate.execute("delete from \"topics\"");
        jdbcTemplate.execute("delete from \"rawis\"");
        jdbcTemplate.execute("delete from \"muhaddiths\"");
        jdbcTemplate.execute("delete from \"ruling\"");
        userRepository.deleteAll();
    }

    @Test
    void publicDetailsShouldReturnMobileSafeShapeForAnonymousViewer() throws Exception {
        TestData data = seedDetailedHadith();
        Statistics statistics = statistics();
        statistics.clear();

        MvcResult result = mockMvc.perform(get("/ahadith/{id}", data.hadithId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().exists("Vary"))
                .andExpect(jsonPath("$.id").value(data.hadithId().toString()))
                .andExpect(jsonPath("$.text").value("main text tashkeel"))
                .andExpect(jsonPath("$.normalText").value("main text normal"))
                .andExpect(jsonPath("$.hadithNumber").value(1))
                .andExpect(jsonPath("$.type").value("marfu"))
                .andExpect(jsonPath("$.sanad").value("main sanad"))
                .andExpect(jsonPath("$.muhaddith.id").value(data.muhaddithId().toString()))
                .andExpect(jsonPath("$.muhaddith.name").value("Main Muhaddith"))
                .andExpect(jsonPath("$.rawi.id").value(data.rawiId().toString()))
                .andExpect(jsonPath("$.book.id").value(data.bookId().toString()))
                .andExpect(jsonPath("$.ruling.id").value(data.rulingId().toString()))
                .andExpect(jsonPath("$.topics[*].id", contains(data.topicFirstId().toString(), data.topicSecondId().toString())))
                .andExpect(jsonPath("$.explanation.id").value(data.explanationId().toString()))
                .andExpect(jsonPath("$.explanation.text").value("explanation text"))
                .andExpect(jsonPath("$.explanation.normalText").value("explanation normal"))
                .andExpect(jsonPath("$.validAlternative.id").value(data.alternativeHadithId().toString()))
                .andExpect(jsonPath("$.validAlternative.text").value("alternative text"))
                .andExpect(jsonPath("$.validAlternative.normalText").value("alternative normal"))
                .andExpect(jsonPath("$.validAlternative.hadithNumber").value(25))
                .andExpect(jsonPath("$.validAlternative.type").value("marfu"))
                .andExpect(jsonPath("$.validAlternative.sanad").value("alternative sanad"))
                .andExpect(jsonPath("$.validAlternative.muhaddith.id").value(data.alternativeMuhaddithId().toString()))
                .andExpect(jsonPath("$.validAlternative.rawi.id").value(data.alternativeRawiId().toString()))
                .andExpect(jsonPath("$.validAlternative.book.id").value(data.alternativeBookId().toString()))
                .andExpect(jsonPath("$.validAlternative.ruling.id").value(data.alternativeRulingId().toString()))
                .andExpect(jsonPath("$.commentsCount").value(3))
                .andExpect(jsonPath("$.viewerState", nullValue()))
                .andExpect(jsonPath("$.hasExplanation").doesNotExist())
                .andExpect(jsonPath("$.hasSubValid").doesNotExist())
                .andExpect(jsonPath("$.subValid").doesNotExist())
                .andExpect(jsonPath("$.searchText").doesNotExist())
                .andExpect(jsonPath("$.createdBy").doesNotExist())
                .andExpect(jsonPath("$.updatedBy").doesNotExist())
                .andReturn();

        assertThat(result.getResponse().getHeaders("Vary")).contains(HttpHeaders.AUTHORIZATION);
        String response = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        assertReferenceOnly(root.get("muhaddith"));
        assertReferenceOnly(root.get("rawi"));
        assertReferenceOnly(root.get("book"));
        assertReferenceOnly(root.get("ruling"));
        for (JsonNode topic : root.get("topics")) {
            assertReferenceOnly(topic);
        }
        JsonNode alternative = root.get("validAlternative");
        assertThat(alternative.has("topics")).isFalse();
        assertThat(alternative.has("explanation")).isFalse();
        assertThat(alternative.has("validAlternative")).isFalse();
        assertThat(alternative.has("commentsCount")).isFalse();
        assertThat(alternative.has("viewerState")).isFalse();
        assertThat(alternative.has("hasSubValid")).isFalse();
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(4);
    }

    @Test
    void publicDetailsShouldReturnEmptyTopicsNullExplanationNullAlternativeAndZeroComments() throws Exception {
        UUID hadithId = uuid(101);
        insertHadith(hadithId, "plain text", "plain normal", 7, null, null, null, null, null, null);

        mockMvc.perform(get("/ahadith/{id}", hadithId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topics", empty()))
                .andExpect(jsonPath("$.explanation", nullValue()))
                .andExpect(jsonPath("$.validAlternative", nullValue()))
                .andExpect(jsonPath("$.commentsCount").value(0))
                .andExpect(jsonPath("$.viewerState", nullValue()));
    }

    @Test
    void publicDetailsShouldReturnViewerStateForAuthenticatedViewer() throws Exception {
        TestData data = seedDetailedHadith();
        User favoritingUser = saveUser("favorited");
        User otherUser = saveUser("not-favorited");
        jdbcTemplate.update("insert into \"favorites\" (\"id\", \"user_id\", \"hadith\") values (?, ?, ?)",
                uuid(90), favoritingUser.getId(), data.hadithId());

        mockMvc.perform(get("/ahadith/{id}", data.hadithId())
                        .header("Authorization", "Bearer " + jwtService.generateAccessToken(favoritingUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewerState.favorited").value(true));

        mockMvc.perform(get("/ahadith/{id}", data.hadithId())
                        .header("Authorization", "Bearer " + jwtService.generateAccessToken(otherUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewerState.favorited").value(false));
    }

    @Test
    void publicDetailsShouldReturnNotFoundForMissingHadith() throws Exception {
        mockMvc.perform(get("/ahadith/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminHadithDetailsShouldRemainSeparatedFromPublicDetails() throws Exception {
        UUID hadithId = uuid(201);
        insertHadith(hadithId, "admin text", "admin normal", 1, null, null, null, null, null, null);
        User admin = saveAdmin();

        mockMvc.perform(get("/admin/ahadith/{id}", hadithId)
                        .header("Authorization", "Bearer " + jwtService.generateAccessToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.searchText").exists())
                .andExpect(jsonPath("$.validAlternative").doesNotExist())
                .andExpect(jsonPath("$.viewerState").doesNotExist());
    }

    private TestData seedDetailedHadith() {
        UUID muhaddithId = uuid(1);
        UUID rawiId = uuid(2);
        UUID rulingId = uuid(3);
        UUID bookId = uuid(4);
        UUID explanationId = uuid(5);
        UUID hadithId = uuid(6);
        UUID topicSecondId = uuid(8);
        UUID topicFirstId = uuid(7);
        UUID alternativeMuhaddithId = uuid(11);
        UUID alternativeRawiId = uuid(12);
        UUID alternativeRulingId = uuid(13);
        UUID alternativeBookId = uuid(14);
        UUID alternativeHadithId = uuid(15);

        insertMuhaddith(muhaddithId, "Main Muhaddith");
        insertRawi(rawiId, "Main Rawi");
        insertRuling(rulingId, "Main Ruling");
        insertBook(bookId, "Main Book", muhaddithId);
        insertExplanation(explanationId, "explanation text", "explanation normal");
        insertMuhaddith(alternativeMuhaddithId, "Alternative Muhaddith");
        insertRawi(alternativeRawiId, "Alternative Rawi");
        insertRuling(alternativeRulingId, "Alternative Ruling");
        insertBook(alternativeBookId, "Alternative Book", alternativeMuhaddithId);
        insertHadith(alternativeHadithId, "alternative text", "alternative normal", 25,
                alternativeBookId, alternativeRawiId, alternativeRulingId, null, "alternative sanad", null);
        insertHadith(hadithId, "main text tashkeel", "main text normal", 1,
                bookId, rawiId, rulingId, explanationId, "main sanad", alternativeHadithId);
        insertTopic(topicSecondId, "A Topic");
        insertTopic(topicFirstId, "A Topic");
        insertTopicClass(uuid(31), hadithId, topicSecondId);
        insertTopicClass(uuid(30), hadithId, topicFirstId);
        insertComment(uuid(41), hadithId, "one");
        insertComment(uuid(42), hadithId, "two");
        insertComment(uuid(43), hadithId, "three");

        return new TestData(
                hadithId,
                alternativeHadithId,
                muhaddithId,
                rawiId,
                rulingId,
                bookId,
                explanationId,
                topicFirstId,
                topicSecondId,
                alternativeMuhaddithId,
                alternativeRawiId,
                alternativeRulingId,
                alternativeBookId);
    }

    private void assertReferenceOnly(JsonNode node) {
        assertThat(node.size()).isEqualTo(2);
        assertThat(node.has("id")).isTrue();
        assertThat(node.has("name")).isTrue();
        assertThat(node.has("createdBy")).isFalse();
        assertThat(node.has("updatedBy")).isFalse();
        assertThat(node.has("createdAt")).isFalse();
        assertThat(node.has("updatedAt")).isFalse();
        assertThat(node.has("searchText")).isFalse();
        assertThat(node.has("password")).isFalse();
    }

    private Statistics statistics() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        return statistics;
    }

    private User saveUser(String suffix) {
        User user = new User();
        user.setName("User " + suffix);
        user.setEmail("details-" + suffix + "-" + UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setType(UserType.member);
        user.setStatus(UserStatus.active);
        return userRepository.save(user);
    }

    private User saveAdmin() {
        User user = saveUser("admin");
        user.setType(UserType.admin);
        return userRepository.save(user);
    }

    private void insertMuhaddith(UUID id, String name) {
        jdbcTemplate.update("insert into \"muhaddiths\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertRawi(UUID id, String name) {
        jdbcTemplate.update("insert into \"rawis\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertRuling(UUID id, String name) {
        jdbcTemplate.update("insert into \"ruling\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertBook(UUID id, String name, UUID muhaddithId) {
        jdbcTemplate.update("insert into \"books\" (\"id\", \"name\", \"muhaddith\") values (?, ?, ?)", id, name, muhaddithId);
    }

    private void insertExplanation(UUID id, String text, String normalText) {
        jdbcTemplate.update("insert into \"explaining\" (\"id\", \"text\", \"normal_text\") values (?, ?, ?)",
                id, text, normalText);
    }

    private void insertHadith(UUID id, String text, String normalText, int number, UUID bookId, UUID rawiId,
                              UUID rulingId, UUID explanationId, String sanad, UUID subValidId) {
        jdbcTemplate.update("""
                insert into "ahadith"
                    ("id", "text", "normal_text", "search_text", "hadith_number", "type", "book", "rawi", "ruling", "explaining", "sanad", "sub_valid")
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, text, normalText, "internal search text", number, "marfu", bookId, rawiId, rulingId,
                explanationId, sanad, subValidId);
    }

    private void insertTopic(UUID id, String name) {
        jdbcTemplate.update("insert into \"topics\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertTopicClass(UUID id, UUID hadithId, UUID topicId) {
        jdbcTemplate.update("insert into \"topic_classes\" (\"id\", \"hadith\", \"topic\") values (?, ?, ?)",
                id, hadithId, topicId);
    }

    private void insertComment(UUID id, UUID hadithId, String text) {
        jdbcTemplate.update("insert into \"comments\" (\"id\", \"hadith\", \"text\") values (?, ?, ?)", id, hadithId, text);
    }

    private UUID uuid(int value) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(value));
    }

    private record TestData(
            UUID hadithId,
            UUID alternativeHadithId,
            UUID muhaddithId,
            UUID rawiId,
            UUID rulingId,
            UUID bookId,
            UUID explanationId,
            UUID topicFirstId,
            UUID topicSecondId,
            UUID alternativeMuhaddithId,
            UUID alternativeRawiId,
            UUID alternativeRulingId,
            UUID alternativeBookId) {
    }
}
