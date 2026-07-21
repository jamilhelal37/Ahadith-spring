package com.jamil.ahadith.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiV1CompatibilityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authShouldWorkOnLegacyAndV1Paths() throws Exception {
        String body = "{\"email\":\"unknown-v1@example.com\"}";

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void filtersShouldWorkOnLegacyAliasesAndV1Path() throws Exception {
        mockMvc.perform(get("/filterslist"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/ahadith/search/filters"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/search/filters"))
                .andExpect(status().isOk());
    }

    @Test
    void publicCatalogShouldWorkOnLegacyAndV1Paths() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk());
    }

    @Test
    void meScholarAndAdminV1SecurityShouldUseExpectedAuthorization() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/scholar/questions"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
