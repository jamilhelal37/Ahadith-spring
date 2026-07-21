package com.jamil.ahadith.features.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.features.search.service.SearchFiltersService;
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FiltersListIntegrationTest {
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
    private SearchFiltersService searchFiltersService;

    @BeforeEach
    void cleanDatabase() {
        searchFiltersService.evictReferenceCaches();
        jdbcTemplate.execute("delete from \"topic_classes\"");
        jdbcTemplate.execute("delete from \"ahadith\"");
        jdbcTemplate.execute("delete from \"books\"");
        jdbcTemplate.execute("delete from \"topics\"");
        jdbcTemplate.execute("delete from \"rawis\"");
        jdbcTemplate.execute("delete from \"muhaddiths\"");
        jdbcTemplate.execute("delete from \"ruling\"");
        userRepository.deleteAll();
    }

    @Test
    void filtersListShouldReturnAllReferenceListsWithoutAuthenticationOrPagination() throws Exception {
        UUID rulingFirstId = uuid(1);
        UUID rulingSecondId = uuid(3);
        UUID rawiFirstId = uuid(11);
        UUID rawiSecondId = uuid(13);
        UUID muhaddithFirstId = uuid(21);
        UUID muhaddithSecondId = uuid(23);
        UUID bookFirstId = uuid(31);
        UUID bookSecondId = uuid(33);
        UUID topicFirstId = uuid(41);
        UUID topicSecondId = uuid(43);

        insertRuling(rulingSecondId, "A Ruling");
        insertRuling(rulingFirstId, "A Ruling");
        insertRawi(rawiSecondId, "A Rawi");
        insertRawi(rawiFirstId, "A Rawi");
        insertMuhaddith(muhaddithSecondId, "A Muhaddith");
        insertMuhaddith(muhaddithFirstId, "A Muhaddith");
        insertBook(bookSecondId, "A Book");
        insertBook(bookFirstId, "A Book");
        insertTopic(topicSecondId, "A Topic");
        insertTopic(topicFirstId, "A Topic");

        String response = mockMvc.perform(get("/filterslist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rulings[*].id", contains(rulingFirstId.toString(), rulingSecondId.toString())))
                .andExpect(jsonPath("$.rawis[*].id", contains(rawiFirstId.toString(), rawiSecondId.toString())))
                .andExpect(jsonPath("$.muhaddiths[*].id", contains(muhaddithFirstId.toString(), muhaddithSecondId.toString())))
                .andExpect(jsonPath("$.books[*].id", contains(bookFirstId.toString(), bookSecondId.toString())))
                .andExpect(jsonPath("$.topics[*].id", contains(topicFirstId.toString(), topicSecondId.toString())))
                .andExpect(jsonPath("$.page").doesNotExist())
                .andExpect(jsonPath("$.size").doesNotExist())
                .andExpect(jsonPath("$.pagination").doesNotExist())
                .andExpect(jsonPath("$.totalItems").doesNotExist())
                .andExpect(jsonPath("$.totalPages").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.size()).isEqualTo(5);
        assertReferenceListOnly(root.get("rulings"));
        assertReferenceListOnly(root.get("rawis"));
        assertReferenceListOnly(root.get("muhaddiths"));
        assertReferenceListOnly(root.get("books"));
        assertReferenceListOnly(root.get("topics"));
    }

    @Test
    void filtersListShouldReturnEmptyArraysWhenTablesAreEmpty() throws Exception {
        mockMvc.perform(get("/filterslist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rulings", empty()))
                .andExpect(jsonPath("$.rawis", empty()))
                .andExpect(jsonPath("$.muhaddiths", empty()))
                .andExpect(jsonPath("$.books", empty()))
                .andExpect(jsonPath("$.topics", empty()));
    }

    @Test
    void existingHadithSearchFiltersEndpointShouldStillWork() throws Exception {
        insertRuling(uuid(1), "Sahih");
        insertRawi(uuid(2), "Rawi");
        insertMuhaddith(uuid(3), "Muhaddith");
        insertBook(uuid(4), "Book");
        insertTopic(uuid(5), "Topic");

        mockMvc.perform(get("/ahadith/search/filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rulings").exists())
                .andExpect(jsonPath("$.rawis").exists())
                .andExpect(jsonPath("$.muhaddiths").exists())
                .andExpect(jsonPath("$.books").exists())
                .andExpect(jsonPath("$.topics").exists())
                .andExpect(jsonPath("$.types").exists());
    }

    @Test
    void filtersListShouldOnlyExposeGetAsPublicEndpoint() throws Exception {
        String authorization = "Bearer " + adminAccessToken();

        mockMvc.perform(post("/filterslist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/filterslist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/filterslist"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/filterslist")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(put("/filterslist")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete("/filterslist")
                        .header("Authorization", authorization))
                .andExpect(status().isMethodNotAllowed());
    }

    private void assertReferenceListOnly(JsonNode items) {
        assertThat(items.isArray()).isTrue();
        for (JsonNode item : items) {
            assertThat(item.size()).isEqualTo(2);
            assertThat(item.has("id")).isTrue();
            assertThat(item.has("name")).isTrue();
            assertThat(item.has("createdBy")).isFalse();
            assertThat(item.has("updatedBy")).isFalse();
            assertThat(item.has("createdAt")).isFalse();
            assertThat(item.has("updatedAt")).isFalse();
            assertThat(item.has("about")).isFalse();
            assertThat(item.has("gender")).isFalse();
            assertThat(item.has("muhaddith")).isFalse();
            assertThat(item.has("topicClasses")).isFalse();
            assertThat(item.has("ahadiths")).isFalse();
        }
    }

    private void insertRuling(UUID id, String name) {
        jdbcTemplate.update("insert into \"ruling\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertRawi(UUID id, String name) {
        jdbcTemplate.update("insert into \"rawis\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertMuhaddith(UUID id, String name) {
        jdbcTemplate.update("insert into \"muhaddiths\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertBook(UUID id, String name) {
        jdbcTemplate.update("insert into \"books\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertTopic(UUID id, String name) {
        jdbcTemplate.update("insert into \"topics\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private String adminAccessToken() {
        User user = new User();
        user.setName("Filters Admin");
        user.setEmail("filters-admin-" + UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setType(UserType.admin);
        user.setStatus(UserStatus.active);
        user = userRepository.save(user);
        return jwtService.generateAccessToken(user);
    }

    private UUID uuid(int value) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(value));
    }
}
