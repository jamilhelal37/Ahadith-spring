package com.jamil.ahadith.features.hadith;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        UUID subValidId = uuid(4);
        jdbc.update("insert into public.books (id, name) values (?, ?)", bookId, "HTTP Search Book");
        jdbc.update("""
                insert into public.ahadith (id, text, hadith_number, type, book)
                values (?, ?, ?, cast(? as public.hadith_type), ?)
                """, subValidId, "unmatched substitute", 99, "marfu", bookId);
        jdbc.update("""
                insert into public.ahadith (id, text, normal_text, search_text, hadith_number, type, book, sanad, sub_valid)
                values (?, ?, ?, ?, ?, cast(? as public.hadith_type), ?, ?, ?)
                """, firstId, "truth path", "truth path normal", "truth path search", 1, "marfu", bookId, "HTTP sanad", subValidId);
        jdbc.update("""
                insert into public.ahadith (id, text, hadith_number, type, book)
                values (?, ?, ?, cast(? as public.hadith_type), ?)
                """, secondId, "truth without sanad", 2, "marfu", bookId);

        mockMvc.perform(post("/ahadith/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "query": "truth",
                                  "mode": "EXACT",
                                  "page": 0,
                                  "size": 10,
                                  "sort": "RELEVANCE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", contains(firstId.toString(), secondId.toString())))
                .andExpect(jsonPath("$.items[0].book.id").value(bookId.toString()))
                .andExpect(jsonPath("$.items[0].text").value("truth path"))
                .andExpect(jsonPath("$.items[0].normalText").value("truth path normal"))
                .andExpect(jsonPath("$.items[0].normal_text").doesNotExist())
                .andExpect(jsonPath("$.items[0].searchText").doesNotExist())
                .andExpect(jsonPath("$.items[0].sanad").value("HTTP sanad"))
                .andExpect(jsonPath("$.items[0].hasSubValid").value(true))
                .andExpect(jsonPath("$.items[0].hasExplanation").value(false))
                .andExpect(jsonPath("$.items[0].subValid").doesNotExist())
                .andExpect(jsonPath("$.items[0].subValidId").doesNotExist())
                .andExpect(jsonPath("$.items[1].normalText", nullValue()))
                .andExpect(jsonPath("$.items[1].sanad", nullValue()))
                .andExpect(jsonPath("$.items[1].hasSubValid").value(false))
                .andExpect(jsonPath("$.items[1].subValid").doesNotExist())
                .andExpect(jsonPath("$.items[1].subValidId").doesNotExist())
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.pagination.totalItems").value(2))
                .andExpect(jsonPath("$.pagination.totalPages").value(1))
                .andExpect(jsonPath("$.pagination.hasNext").value(false));

        mockMvc.perform(get("/ahadith/{id}", firstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstId.toString()))
                .andExpect(jsonPath("$.text").value("truth path"))
                .andExpect(jsonPath("$.normalText").value("truth path normal"))
                .andExpect(jsonPath("$.sanad").value("HTTP sanad"))
                .andExpect(jsonPath("$.normal_text").doesNotExist())
                .andExpect(jsonPath("$.searchText").doesNotExist());
    }

    private UUID uuid(int value) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(value));
    }
}
