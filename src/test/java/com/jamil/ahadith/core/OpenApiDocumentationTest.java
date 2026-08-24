package com.jamil.ahadith.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void apiDocsShouldExposeV1BearerSchemeAndNoSensitiveEntitySchemas() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(body);

        assertThat(root.at("/components/securitySchemes/bearer-jwt/scheme").asText())
                .isEqualTo("bearer");
        assertThat(root.path("paths").has("/api/v1/auth/forgot-password")).isTrue();
        assertThat(root.path("paths").has("/api/v1/auth/google")).isTrue();
        assertThat(root.path("paths").path("/api/v1/auth/google").has("post")).isTrue();
        assertThat(root.path("paths").has("/api/v1/admin/users")).isTrue();
        assertThat(root.path("paths").has("/api/v1/admin/users/{id}/status")).isTrue();
        assertThat(root.path("paths").path("/api/v1/admin/users/{id}/status").has("put")).isTrue();
        assertThat(root.path("paths").path("/api/v1/admin/users/{id}/status").has("patch")).isFalse();
        assertThat(root.path("paths").has("/api/v1/me/password")).isTrue();
        assertThat(root.path("paths").path("/api/v1/me/password").has("put")).isTrue();
        assertThat(root.path("paths").fieldNames())
                .toIterable()
                .allSatisfy(path -> assertThat(path.toString()).startsWith("/api/v1/"));

        JsonNode schemasNode = root.path("components").path("schemas");
        assertThat(schemasNode.has("User")).isFalse();
        assertThat(schemasNode.toString()).doesNotContain("searchVector");
        assertThat(schemasNode.toString()).doesNotContain("googleSubject");
    }
}
