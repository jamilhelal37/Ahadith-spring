package com.jamil.ahadith.features.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.core.security.jwt.JwtService;
import com.jamil.ahadith.features.catalog.dto.response.BookResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import com.jamil.ahadith.features.catalog.entity.Book;
import com.jamil.ahadith.features.catalog.entity.Muhaddith;
import com.jamil.ahadith.features.catalog.entity.Rawi;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.user.entity.Gender;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicMobileCatalogTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MuhaddithRepository muhaddithRepository;

    @Autowired
    private RawiRepository rawiRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private HadithRepository hadithRepository;

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
        hadithRepository.deleteAll();
        bookRepository.deleteAll();
        jdbcTemplate.execute("delete from \"explaining\"");
        jdbcTemplate.execute("delete from \"topics\"");
        jdbcTemplate.execute("delete from \"ruling\"");
        rawiRepository.deleteAll();
        muhaddithRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void publicMuhaddithsShouldReturnCompleteArraySortedWithSerialNumbersAndOnlyPublicFields() throws Exception {
        saveMuhaddith("B Scholar", "about b");
        saveMuhaddith(UUID.fromString("00000000-0000-0000-0000-000000000003"), "A Scholar", "about a later id");
        saveMuhaddith(UUID.fromString("00000000-0000-0000-0000-000000000001"), "A Scholar", null);
        saveMuhaddith("C Scholar", "about c");

        String response = mockMvc.perform(get("/muhaddiths"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$.items").doesNotExist())
                .andExpect(jsonPath("$.pagination").doesNotExist())
                .andExpect(jsonPath("$[*].serialNumber", contains(1, 2, 3, 4)))
                .andExpect(jsonPath("$[*].name", contains("A Scholar", "A Scholar", "B Scholar", "C Scholar")))
                .andExpect(jsonPath("$[0].about", nullValue()))
                .andExpect(jsonPath("$[1].about").value("about a later id"))
                .andExpect(jsonPath("$[2].about").value("about b"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.isArray()).isTrue();
        for (JsonNode item : root) {
            assertThat(item.size()).isEqualTo(3);
            assertThat(item.has("serialNumber")).isTrue();
            assertThat(item.has("name")).isTrue();
            assertThat(item.has("about")).isTrue();
            assertThat(item.has("id")).isFalse();
            assertThat(item.has("gender")).isFalse();
            assertThat(item.has("createdBy")).isFalse();
            assertThat(item.has("updatedBy")).isFalse();
            assertThat(item.has("createdAt")).isFalse();
            assertThat(item.has("updatedAt")).isFalse();
        }
    }

    @Test
    void publicRawisShouldReturnCompleteArraySortedWithSerialNumbersAndOnlyPublicFields() throws Exception {
        saveRawi("B Rawi", "about b");
        saveRawi(UUID.fromString("00000000-0000-0000-0000-000000000003"), "A Rawi", "about a later id");
        saveRawi(UUID.fromString("00000000-0000-0000-0000-000000000001"), "A Rawi", null);
        saveRawi("C Rawi", "about c");

        String response = mockMvc.perform(get("/rawis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$.items").doesNotExist())
                .andExpect(jsonPath("$.pagination").doesNotExist())
                .andExpect(jsonPath("$[*].serialNumber", contains(1, 2, 3, 4)))
                .andExpect(jsonPath("$[*].name", contains("A Rawi", "A Rawi", "B Rawi", "C Rawi")))
                .andExpect(jsonPath("$[0].about", nullValue()))
                .andExpect(jsonPath("$[1].about").value("about a later id"))
                .andExpect(jsonPath("$[2].about").value("about b"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.isArray()).isTrue();
        for (JsonNode item : root) {
            assertThat(item.size()).isEqualTo(3);
            assertThat(item.has("serialNumber")).isTrue();
            assertThat(item.has("name")).isTrue();
            assertThat(item.has("about")).isTrue();
            assertThat(item.has("id")).isFalse();
            assertThat(item.has("gender")).isFalse();
            assertThat(item.has("createdBy")).isFalse();
            assertThat(item.has("updatedBy")).isFalse();
            assertThat(item.has("createdAt")).isFalse();
            assertThat(item.has("updatedAt")).isFalse();
        }
    }

    @Test
    void adminRawiGetEndpointsShouldStillReturnAdminResponseDtoShape() throws Exception {
        Rawi rawi = saveRawi("Admin Rawi", "admin about text");
        String token = adminAccessToken();

        String listResponse = mockMvc.perform(get("/admin/rawis")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(rawi.getId().toString()))
                .andExpect(jsonPath("$.items[0].name").value("Admin Rawi"))
                .andExpect(jsonPath("$.items[0].gender").value("male"))
                .andExpect(jsonPath("$.items[0].about").value("admin about text"))
                .andExpect(jsonPath("$.pagination").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode adminListItem = objectMapper.readTree(listResponse).get("items").get(0);
        assertThat(adminListItem.has("id")).isTrue();
        assertThat(adminListItem.has("gender")).isTrue();
        assertThat(adminListItem.has("createdBy")).isTrue();
        assertThat(adminListItem.has("updatedBy")).isTrue();
        assertThat(adminListItem.has("createdAt")).isTrue();
        assertThat(adminListItem.has("updatedAt")).isTrue();

        String detailResponse = mockMvc.perform(get("/admin/rawis/{id}", rawi.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rawi.getId().toString()))
                .andExpect(jsonPath("$.name").value("Admin Rawi"))
                .andExpect(jsonPath("$.gender").value("male"))
                .andExpect(jsonPath("$.about").value("admin about text"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode adminDetail = objectMapper.readTree(detailResponse);
        assertThat(adminDetail.has("id")).isTrue();
        assertThat(adminDetail.has("gender")).isTrue();
        assertThat(adminDetail.has("createdBy")).isTrue();
        assertThat(adminDetail.has("updatedBy")).isTrue();
        assertThat(adminDetail.has("createdAt")).isTrue();
        assertThat(adminDetail.has("updatedAt")).isTrue();
    }

    @Test
    void adminMuhaddithGetEndpointsShouldStillReturnAdminResponseDtoShape() throws Exception {
        Muhaddith muhaddith = saveMuhaddith("Admin Muhaddith", "admin muhaddith about");
        String token = adminAccessToken();

        String listResponse = mockMvc.perform(get("/admin/muhaddiths")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(muhaddith.getId().toString()))
                .andExpect(jsonPath("$.items[0].name").value("Admin Muhaddith"))
                .andExpect(jsonPath("$.items[0].gender").value("male"))
                .andExpect(jsonPath("$.items[0].about").value("admin muhaddith about"))
                .andExpect(jsonPath("$.pagination").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode adminListItem = objectMapper.readTree(listResponse).get("items").get(0);
        assertThat(adminListItem.has("id")).isTrue();
        assertThat(adminListItem.has("gender")).isTrue();
        assertThat(adminListItem.has("createdBy")).isTrue();
        assertThat(adminListItem.has("updatedBy")).isTrue();
        assertThat(adminListItem.has("createdAt")).isTrue();
        assertThat(adminListItem.has("updatedAt")).isTrue();

        String detailResponse = mockMvc.perform(get("/admin/muhaddiths/{id}", muhaddith.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(muhaddith.getId().toString()))
                .andExpect(jsonPath("$.name").value("Admin Muhaddith"))
                .andExpect(jsonPath("$.gender").value("male"))
                .andExpect(jsonPath("$.about").value("admin muhaddith about"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode adminDetail = objectMapper.readTree(detailResponse);
        assertThat(adminDetail.has("id")).isTrue();
        assertThat(adminDetail.has("gender")).isTrue();
        assertThat(adminDetail.has("createdBy")).isTrue();
        assertThat(adminDetail.has("updatedBy")).isTrue();
        assertThat(adminDetail.has("createdAt")).isTrue();
        assertThat(adminDetail.has("updatedAt")).isTrue();
    }

    @Test
    void publicBooksShouldBeAccessibleSortedWithMuhaddithDataAndNullSafeMissingMuhaddith() throws Exception {
        Muhaddith muhaddith = saveMuhaddith("Book Author", "author bio");
        Book secondA = saveBook(UUID.fromString("00000000-0000-0000-0000-000000000003"), "A Book", muhaddith);
        Book firstA = saveBook(UUID.fromString("00000000-0000-0000-0000-000000000001"), "A Book", null);
        Book bBook = saveBook("B Book", null);
        Book cBook = saveBook("C Book", null);

        String response = mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$.items").doesNotExist())
                .andExpect(jsonPath("$.pagination").doesNotExist())
                .andExpect(jsonPath("$[*].name", contains("A Book", "A Book", "B Book", "C Book")))
                .andExpect(jsonPath("$[*].id", contains(
                        firstA.getId().toString(),
                        secondA.getId().toString(),
                        bBook.getId().toString(),
                        cBook.getId().toString())))
                .andExpect(jsonPath("$[0].muhaddith", nullValue()))
                .andExpect(jsonPath("$[1].muhaddith.id").value(muhaddith.getId().toString()))
                .andExpect(jsonPath("$[1].muhaddith.name").value("Book Author"))
                .andExpect(jsonPath("$[1].muhaddith.about").doesNotExist())
                .andExpect(jsonPath("$[1].muhaddith.gender").doesNotExist())
                .andExpect(jsonPath("$[1].muhaddith.createdBy").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.isArray()).isTrue();
        for (JsonNode item : root) {
            assertThat(item.size()).isEqualTo(3);
            assertThat(item.has("id")).isTrue();
            assertThat(item.has("name")).isTrue();
            assertThat(item.has("muhaddith")).isTrue();
            assertThat(item.has("serialNumber")).isFalse();
            assertThat(item.has("muhaddithId")).isFalse();
            assertThat(item.has("muhaddithName")).isFalse();
            assertThat(item.has("createdBy")).isFalse();
            assertThat(item.has("updatedBy")).isFalse();
            assertThat(item.has("createdAt")).isFalse();
            assertThat(item.has("updatedAt")).isFalse();
            if (item.get("muhaddith").isObject()) {
                assertThat(item.get("muhaddith").size()).isEqualTo(2);
                assertThat(item.get("muhaddith").has("id")).isTrue();
                assertThat(item.get("muhaddith").has("name")).isTrue();
            }
        }

        String detailResponse = mockMvc.perform(get("/books/{id}", secondA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(secondA.getId().toString()))
                .andExpect(jsonPath("$.name").value("A Book"))
                .andExpect(jsonPath("$.muhaddith.id").value(muhaddith.getId().toString()))
                .andExpect(jsonPath("$.muhaddith.name").value("Book Author"))
                .andExpect(jsonPath("$.muhaddithId").doesNotExist())
                .andExpect(jsonPath("$.muhaddithName").doesNotExist())
                .andExpect(jsonPath("$.createdBy").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode detail = objectMapper.readTree(detailResponse);
        assertThat(detail.size()).isEqualTo(3);

        mockMvc.perform(get("/books/{id}", firstA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.muhaddith", nullValue()));
    }

    @Test
    void publicBookDetailsShouldReturnNotFoundForMissingBook() throws Exception {
        mockMvc.perform(get("/books/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminBooksShouldReturnAdminBookResponseWithMuhaddithReference() throws Exception {
        Muhaddith muhaddith = saveMuhaddith("Admin Book Author", "private bio");
        Book book = saveBook("Admin Book", muhaddith);
        String token = adminAccessToken();

        assertThat(BookResponseDto.class.getDeclaredField("muhaddith").getType())
                .isEqualTo(MuhaddithReferenceResponseDto.class);

        String listResponse = mockMvc.perform(get("/admin/books")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(book.getId().toString()))
                .andExpect(jsonPath("$.items[0].name").value("Admin Book"))
                .andExpect(jsonPath("$.items[0].muhaddith.id").value(muhaddith.getId().toString()))
                .andExpect(jsonPath("$.items[0].muhaddith.name").value("Admin Book Author"))
                .andExpect(jsonPath("$.items[0].muhaddith.about").doesNotExist())
                .andExpect(jsonPath("$.items[0].muhaddith.gender").doesNotExist())
                .andExpect(jsonPath("$.items[0].createdBy", nullValue()))
                .andExpect(jsonPath("$.items[0].updatedBy", nullValue()))
                .andExpect(jsonPath("$.items[0].createdAt", nullValue()))
                .andExpect(jsonPath("$.items[0].updatedAt", nullValue()))
                .andExpect(jsonPath("$.pagination").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode adminListItem = objectMapper.readTree(listResponse).get("items").get(0);
        assertThat(adminListItem.has("muhaddith")).isTrue();
        assertThat(adminListItem.get("muhaddith").size()).isEqualTo(2);
        assertThat(adminListItem.has("createdBy")).isTrue();
        assertThat(adminListItem.has("updatedBy")).isTrue();
        assertThat(adminListItem.has("createdAt")).isTrue();
        assertThat(adminListItem.has("updatedAt")).isTrue();

        String detailResponse = mockMvc.perform(get("/admin/books/{id}", book.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book.getId().toString()))
                .andExpect(jsonPath("$.name").value("Admin Book"))
                .andExpect(jsonPath("$.muhaddith.id").value(muhaddith.getId().toString()))
                .andExpect(jsonPath("$.muhaddith.name").value("Admin Book Author"))
                .andExpect(jsonPath("$.muhaddith.about").doesNotExist())
                .andExpect(jsonPath("$.createdBy", nullValue()))
                .andExpect(jsonPath("$.updatedBy", nullValue()))
                .andExpect(jsonPath("$.createdAt", nullValue()))
                .andExpect(jsonPath("$.updatedAt", nullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode adminDetail = objectMapper.readTree(detailResponse);
        assertThat(adminDetail.has("muhaddith")).isTrue();
        assertThat(adminDetail.get("muhaddith").size()).isEqualTo(2);
        assertThat(adminDetail.has("createdBy")).isTrue();
        assertThat(adminDetail.has("updatedBy")).isTrue();
        assertThat(adminDetail.has("createdAt")).isTrue();
        assertThat(adminDetail.has("updatedAt")).isTrue();
    }

    @Test
    void publicBookAhadithShouldPageOnlyRequestedBookWithStableHadithNumberOrder() throws Exception {
        Muhaddith muhaddith = saveMuhaddith("Book Muhaddith", "about");
        Rawi rawi = saveRawi("Book Rawi", "about");
        UUID rulingId = uuid(301);
        UUID explanationId = uuid(302);
        UUID topicBId = uuid(303);
        UUID topicAId = uuid(304);
        insertRuling(rulingId, "Book Ruling");
        insertExplanation(explanationId, "book explanation");
        insertTopic(topicBId, "B Topic");
        insertTopic(topicAId, "A Topic");

        Book requestedBook = saveBook("Requested", muhaddith);
        Book otherBook = saveBook("Other", null);
        Hadith third = saveHadith(uuid(3), requestedBook, 3, "third");
        Hadith second = saveHadith(uuid(2), requestedBook, 2, "second");
        Hadith first = saveHadith(uuid(1), requestedBook, 1, "first");
        saveHadith(uuid(4), otherBook, 1, "other book");
        Hadith subValid = saveHadith(uuid(5), otherBook, 99, "sub valid");

        jdbcTemplate.update(
                "update \"ahadith\" set \"normal_text\" = ?, \"search_text\" = ?, \"sanad\" = ?, \"sub_valid\" = ?, \"rawi\" = ?, \"ruling\" = ? where \"id\" = ?",
                "first normal",
                "search-only-value",
                "book sanad",
                subValid.getId(),
                rawi.getId(),
                rulingId,
                first.getId());
        insertTopicClass(uuid(305), first.getId(), topicBId);
        insertTopicClass(uuid(306), first.getId(), topicAId);
        jdbcTemplate.update(
                "update \"ahadith\" set \"normal_text\" = ?, \"explaining\" = ? where \"id\" = ?",
                "second normal",
                explanationId,
                second.getId());

        List<Hadith> expected = List.of(third, second, first)
                .stream()
                .sorted(Comparator.comparing(Hadith::getHadithNumber)
                        .thenComparing(Hadith::getId))
                .toList();

        String response = mockMvc.perform(get("/books/{bookId}/ahadith", requestedBook.getId())
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id").value(expected.get(0).getId().toString()))
                .andExpect(jsonPath("$.items[1].id").value(expected.get(1).getId().toString()))
                .andExpect(jsonPath("$.items[0].text").value("first"))
                .andExpect(jsonPath("$.items[0].normalText").value("first normal"))
                .andExpect(jsonPath("$.items[0].normal_text").doesNotExist())
                .andExpect(jsonPath("$.items[0].searchText").doesNotExist())
                .andExpect(jsonPath("$.items[0].sanad").value("book sanad"))
                .andExpect(jsonPath("$.items[0].hasSubValid").value(true))
                .andExpect(jsonPath("$.items[0].hasExplanation").value(false))
                .andExpect(jsonPath("$.items[0].subValid").doesNotExist())
                .andExpect(jsonPath("$.items[0].subValidId").doesNotExist())
                .andExpect(jsonPath("$.items[0].explanation").doesNotExist())
                .andExpect(jsonPath("$.items[0].validAlternative").doesNotExist())
                .andExpect(jsonPath("$.items[0].commentsCount").doesNotExist())
                .andExpect(jsonPath("$.items[0].viewerState").doesNotExist())
                .andExpect(jsonPath("$.items[0].book.id").value(requestedBook.getId().toString()))
                .andExpect(jsonPath("$.items[0].book.name").value("Requested"))
                .andExpect(jsonPath("$.items[0].rawi.id").value(rawi.getId().toString()))
                .andExpect(jsonPath("$.items[0].rawi.name").value("Book Rawi"))
                .andExpect(jsonPath("$.items[0].ruling.id").value(rulingId.toString()))
                .andExpect(jsonPath("$.items[0].ruling.name").value("Book Ruling"))
                .andExpect(jsonPath("$.items[0].muhaddith.id").value(muhaddith.getId().toString()))
                .andExpect(jsonPath("$.items[0].muhaddith.name").value("Book Muhaddith"))
                .andExpect(jsonPath("$.items[0].topics[*].id", contains(topicAId.toString(), topicBId.toString())))
                .andExpect(jsonPath("$.items[1].normalText").value("second normal"))
                .andExpect(jsonPath("$.items[1].sanad", nullValue()))
                .andExpect(jsonPath("$.items[1].hasSubValid").value(false))
                .andExpect(jsonPath("$.items[1].hasExplanation").value(true))
                .andExpect(jsonPath("$.items[1].topics", hasSize(0)))
                .andExpect(jsonPath("$.items[1].subValid").doesNotExist())
                .andExpect(jsonPath("$.items[1].subValidId").doesNotExist())
                .andExpect(jsonPath("$.items[*].book.id",
                        contains(requestedBook.getId().toString(), requestedBook.getId().toString())))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(2))
                .andExpect(jsonPath("$.pagination.totalItems").value(3))
                .andExpect(jsonPath("$.pagination.totalPages").value(2))
                .andExpect(jsonPath("$.pagination.hasNext").value(true))
                .andExpect(jsonPath("$.pagination.hasPrevious").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode firstItem = objectMapper.readTree(response).get("items").get(0);
        assertThat(firstItem.size()).isEqualTo(13);
        assertReferenceOnly(firstItem.get("book"));
        assertReferenceOnly(firstItem.get("rawi"));
        assertReferenceOnly(firstItem.get("ruling"));
        assertReferenceOnly(firstItem.get("muhaddith"));
        for (JsonNode topic : firstItem.get("topics")) {
            assertReferenceOnly(topic);
        }
        assertThat(firstItem.has("createdBy")).isFalse();
        assertThat(firstItem.has("updatedBy")).isFalse();
        assertThat(firstItem.has("createdAt")).isFalse();
        assertThat(firstItem.has("updatedAt")).isFalse();

        mockMvc.perform(get("/books/{bookId}/ahadith", requestedBook.getId())
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(expected.get(2).getId().toString()))
                .andExpect(jsonPath("$.items[0].normalText", nullValue()))
                .andExpect(jsonPath("$.items[0].sanad", nullValue()))
                .andExpect(jsonPath("$.items[0].hasSubValid").value(false))
                .andExpect(jsonPath("$.items[0].topics", hasSize(0)))
                .andExpect(jsonPath("$.items[0].subValid").doesNotExist())
                .andExpect(jsonPath("$.items[0].subValidId").doesNotExist())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.hasNext").value(false))
                .andExpect(jsonPath("$.pagination.hasPrevious").value(true));

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        mockMvc.perform(get("/books/{bookId}/ahadith", requestedBook.getId())
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[0].normalText").value("first normal"))
                .andExpect(jsonPath("$.items[0].hasSubValid").value(true));

        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(5);
    }

    @Test
    void publicBookAhadithShouldReturnEmptyPageForExistingBookWithoutAhadith() throws Exception {
        Book book = saveBook("Empty", null);

        mockMvc.perform(get("/books/{bookId}/ahadith", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(50))
                .andExpect(jsonPath("$.pagination.totalItems").value(0))
                .andExpect(jsonPath("$.pagination.totalPages").value(0))
                .andExpect(jsonPath("$.pagination.hasNext").value(false))
                .andExpect(jsonPath("$.pagination.hasPrevious").value(false));
    }

    @Test
    void publicBookAhadithShouldDefaultAndClampSizeAndRejectInvalidPagination() throws Exception {
        Book book = saveBook("Paged", null);
        for (int i = 1; i <= 55; i++) {
            saveHadith(book, i, "hadith " + i);
        }

        mockMvc.perform(get("/books/{bookId}/ahadith", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(50)))
                .andExpect(jsonPath("$.pagination.size").value(50))
                .andExpect(jsonPath("$.pagination.hasNext").value(true));

        mockMvc.perform(get("/books/{bookId}/ahadith", book.getId()).param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(50)))
                .andExpect(jsonPath("$.pagination.size").value(50));

        mockMvc.perform(get("/books/{bookId}/ahadith", book.getId()).param("page", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/books/{bookId}/ahadith", book.getId()).param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicBookAhadithShouldReturnNotFoundForMissingBook() throws Exception {
        mockMvc.perform(get("/books/{bookId}/ahadith", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private Muhaddith saveMuhaddith(String name, String about) {
        return saveMuhaddith(null, name, about);
    }

    private Muhaddith saveMuhaddith(UUID id, String name, String about) {
        Muhaddith muhaddith = new Muhaddith();
        muhaddith.setName(name);
        muhaddith.setAbout(about);
        muhaddith.setGender(Gender.male);
        muhaddith = muhaddithRepository.saveAndFlush(muhaddith);
        if (id == null) {
            return muhaddith;
        }

        jdbcTemplate.update(
                "update \"muhaddiths\" set \"id\" = ? where \"id\" = ?",
                id,
                muhaddith.getId());
        return muhaddithRepository.findById(id).orElseThrow();
    }

    private Rawi saveRawi(String name, String about) {
        return saveRawi(null, name, about);
    }

    private Rawi saveRawi(UUID id, String name, String about) {
        Rawi rawi = new Rawi();
        rawi.setName(name);
        rawi.setAbout(about);
        rawi.setGender(Gender.male);
        rawi = rawiRepository.saveAndFlush(rawi);
        if (id == null) {
            return rawi;
        }

        jdbcTemplate.update(
                "update \"rawis\" set \"id\" = ? where \"id\" = ?",
                id,
                rawi.getId());
        return rawiRepository.findById(id).orElseThrow();
    }

    private String adminAccessToken() {
        User user = new User();
        user.setName("Catalog Admin");
        user.setEmail("catalog-admin-" + UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setType(UserType.admin);
        user.setStatus(UserStatus.active);
        user = userRepository.save(user);
        return jwtService.generateAccessToken(user);
    }

    private Book saveBook(String name, Muhaddith muhaddith) {
        return saveBook(null, name, muhaddith);
    }

    private Book saveBook(UUID id, String name, Muhaddith muhaddith) {
        Book book = new Book();
        book.setName(name);
        book.setMuhaddith(muhaddith);
        book = bookRepository.saveAndFlush(book);
        if (id == null) {
            return book;
        }

        jdbcTemplate.update(
                "update \"books\" set \"id\" = ? where \"id\" = ?",
                id,
                book.getId());
        return bookRepository.findById(id).orElseThrow();
    }

    private Hadith saveHadith(Book book, int hadithNumber, String text) {
        return saveHadith(null, book, hadithNumber, text);
    }

    private Hadith saveHadith(UUID id, Book book, int hadithNumber, String text) {
        Hadith hadith = new Hadith();
        hadith.setId(id);
        hadith.setBook(book);
        hadith.setHadithNumber(hadithNumber);
        hadith.setText(text);
        return hadithRepository.save(hadith);
    }

    private void insertRuling(UUID id, String name) {
        jdbcTemplate.update("insert into \"ruling\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertExplanation(UUID id, String text) {
        jdbcTemplate.update("insert into \"explaining\" (\"id\", \"text\") values (?, ?)", id, text);
    }

    private void insertTopic(UUID id, String name) {
        jdbcTemplate.update("insert into \"topics\" (\"id\", \"name\") values (?, ?)", id, name);
    }

    private void insertTopicClass(UUID id, UUID hadithId, UUID topicId) {
        jdbcTemplate.update("insert into \"topic_classes\" (\"id\", \"hadith\", \"topic\") values (?, ?, ?)",
                id, hadithId, topicId);
    }

    private void assertReferenceOnly(JsonNode node) {
        assertThat(node.size()).isEqualTo(2);
        assertThat(node.has("id")).isTrue();
        assertThat(node.has("name")).isTrue();
    }

    private UUID uuid(int value) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(value));
    }
}
