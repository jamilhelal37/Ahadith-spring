package com.jamil.ahadith;

import com.jamil.ahadith.entities.Book;
import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.Muhaddith;
import com.jamil.ahadith.entities.Rawi;
import com.jamil.ahadith.repositories.BookRepository;
import com.jamil.ahadith.repositories.HadithRepository;
import com.jamil.ahadith.repositories.MuhaddithRepository;
import com.jamil.ahadith.repositories.RawiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
    private MuhaddithRepository muhaddithRepository;

    @Autowired
    private RawiRepository rawiRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private HadithRepository hadithRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        hadithRepository.deleteAll();
        bookRepository.deleteAll();
        rawiRepository.deleteAll();
        muhaddithRepository.deleteAll();
    }

    @Test
    void publicMuhaddithsShouldBeAccessibleSortedWithSerialNumbersAndAbout() throws Exception {
        saveMuhaddith("B Scholar", "about b");
        saveMuhaddith("A Scholar", null);
        saveMuhaddith("C Scholar", "about c");

        mockMvc.perform(get("/muhaddiths"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].serialNumber", contains(1, 2, 3)))
                .andExpect(jsonPath("$[*].name", contains("A Scholar", "B Scholar", "C Scholar")))
                .andExpect(jsonPath("$[0].about", nullValue()))
                .andExpect(jsonPath("$[1].about").value("about b"));
    }

    @Test
    void publicRawisShouldBeAccessibleSortedWithSerialNumbersAndAbout() throws Exception {
        saveRawi("B Rawi", "about b");
        saveRawi("A Rawi", null);
        saveRawi("C Rawi", "about c");

        mockMvc.perform(get("/rawis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].serialNumber", contains(1, 2, 3)))
                .andExpect(jsonPath("$[*].name", contains("A Rawi", "B Rawi", "C Rawi")))
                .andExpect(jsonPath("$[0].about", nullValue()))
                .andExpect(jsonPath("$[1].about").value("about b"));
    }

    @Test
    void publicBooksShouldBeAccessibleSortedWithMuhaddithDataAndNullSafeMissingMuhaddith() throws Exception {
        Muhaddith muhaddith = saveMuhaddith("Book Author", "author bio");
        saveBook("C Book", null);
        Book bookWithMuhaddith = saveBook("A Book", muhaddith);
        saveBook("B Book", null);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].serialNumber", contains(1, 2, 3)))
                .andExpect(jsonPath("$[*].name", contains("A Book", "B Book", "C Book")))
                .andExpect(jsonPath("$[0].id").value(bookWithMuhaddith.getId().toString()))
                .andExpect(jsonPath("$[0].muhaddithId").value(muhaddith.getId().toString()))
                .andExpect(jsonPath("$[0].muhaddithName").value("Book Author"))
                .andExpect(jsonPath("$[1].muhaddithId", nullValue()))
                .andExpect(jsonPath("$[1].muhaddithName", nullValue()));
    }

    @Test
    void publicBookAhadithShouldPageOnlyRequestedBookWithStableHadithNumberOrder() throws Exception {
        Book requestedBook = saveBook("Requested", null);
        Book otherBook = saveBook("Other", null);
        LocalDateTime baseCreatedAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        Hadith second = saveHadith(
                UUID.fromString("00000000-0000-0000-0000-000000000003"), requestedBook, 2, "second");
        Hadith firstTieB = saveHadith(
                UUID.fromString("00000000-0000-0000-0000-000000000002"), requestedBook, 1, "first tie b");
        Hadith firstTieA = saveHadith(
                UUID.fromString("00000000-0000-0000-0000-000000000001"), requestedBook, 1, "first tie a");
        saveHadith(UUID.fromString("00000000-0000-0000-0000-000000000004"), otherBook, 1, "other book");

        second = setHadithCreatedAt(second.getId(), baseCreatedAt.plusMinutes(2));
        firstTieB = setHadithCreatedAt(firstTieB.getId(), baseCreatedAt.plusMinutes(1));
        firstTieA = setHadithCreatedAt(firstTieA.getId(), baseCreatedAt);

        List<Hadith> expected = List.of(second, firstTieB, firstTieA)
                .stream()
                .sorted(Comparator.comparing(Hadith::getHadithNumber)
                        .thenComparing(Hadith::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Hadith::getId))
                .toList();

        mockMvc.perform(get("/books/{bookId}/ahadith", requestedBook.getId())
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id").value(expected.get(0).getId().toString()))
                .andExpect(jsonPath("$.items[1].id").value(expected.get(1).getId().toString()))
                .andExpect(jsonPath("$.items[*].book.id",
                        contains(requestedBook.getId().toString(), requestedBook.getId().toString())))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(2))
                .andExpect(jsonPath("$.pagination.totalItems").value(3))
                .andExpect(jsonPath("$.pagination.totalPages").value(2))
                .andExpect(jsonPath("$.pagination.hasNext").value(true));

        mockMvc.perform(get("/books/{bookId}/ahadith", requestedBook.getId())
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(expected.get(2).getId().toString()))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.hasNext").value(false));
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
        Muhaddith muhaddith = new Muhaddith();
        muhaddith.setName(name);
        muhaddith.setAbout(about);
        return muhaddithRepository.save(muhaddith);
    }

    private Rawi saveRawi(String name, String about) {
        Rawi rawi = new Rawi();
        rawi.setName(name);
        rawi.setAbout(about);
        return rawiRepository.save(rawi);
    }

    private Book saveBook(String name, Muhaddith muhaddith) {
        Book book = new Book();
        book.setName(name);
        book.setMuhaddith(muhaddith);
        return bookRepository.save(book);
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

    private Hadith setHadithCreatedAt(UUID id, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "update \"ahadith\" set \"created_at\" = ? where \"id\" = ?",
                Timestamp.valueOf(createdAt),
                id);
        return hadithRepository.findById(id).orElseThrow();
    }
}
