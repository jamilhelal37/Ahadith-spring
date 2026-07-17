package com.jamil.ahadith.features.hadith;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.catalog.entity.Book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PublicHadithSearchPostgresIT extends PostgresIntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicSearchEndpointShouldUseRealControllerServiceRepositoryAndPostgres() throws Exception {
        UUID bookId = uuid(1);
        UUID firstId = uuid(2);
        UUID secondId = uuid(3);
        jdbc.update("insert into public.books (id, name) values (?, ?)", bookId, "HTTP Search Book");
        jdbc.update("""
                insert into public.ahadith (id, text, hadith_number, type, book)
                values (?, ?, ?, cast(? as public.hadith_type), ?)
                """, firstId, "الصدق طريق النجاة", 1, "marfu", bookId);
        jdbc.update("""
                insert into public.ahadith (id, text, hadith_number, type, book)
                values (?, ?, ?, cast(? as public.hadith_type), ?)
                """, secondId, "نص غير مطابق", 2, "marfu", bookId);

        mockMvc.perform(post("/ahadith/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "query": "الصدق",
                                  "mode": "FLEXIBLE",
                                  "page": 0,
                                  "size": 10,
                                  "sort": "RELEVANCE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", contains(firstId.toString())))
                .andExpect(jsonPath("$.items[0].book.id").value(bookId.toString()))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.pagination.totalItems").value(1))
                .andExpect(jsonPath("$.pagination.totalPages").value(1))
                .andExpect(jsonPath("$.pagination.hasNext").value(false));
    }

    private UUID uuid(int value) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(value));
    }
}
