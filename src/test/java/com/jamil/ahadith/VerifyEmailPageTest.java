package com.jamil.ahadith;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VerifyEmailPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void verificationPageAndStaticResourcesShouldBePublic() throws Exception {
        mockMvc.perform(get("/verify-email?token=dummy"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));

        mockMvc.perform(get("/verify-email.html")).andExpect(status().isOk());
        mockMvc.perform(get("/verify-email.css")).andExpect(status().isOk());
        mockMvc.perform(get("/verify-email.js")).andExpect(status().isOk());
    }

    @Test
    void verifyEmailPostShouldBePublicButMemberApiShouldStayProtected() throws Exception {
        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/me/favorites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verificationHtmlShouldBeArabicRtlAndReferenceLocalAssets() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/static/verify-email.html"));

        assertThat(html)
                .contains("lang=\"ar\"")
                .contains("dir=\"rtl\"")
                .contains("charset=\"utf-8\"")
                .contains("name=\"robots\" content=\"noindex,nofollow\"")
                .contains("name=\"referrer\" content=\"no-referrer\"")
                .contains("href=\"/verify-email.css\"")
                .contains("src=\"/verify-email.js\"")
                .doesNotContain("token=");
    }

    @Test
    void verificationJavascriptShouldPostTokenAndAvoidBrowserStorage() throws Exception {
        String js = Files.readString(Path.of("src/main/resources/static/verify-email.js"));

        assertThat(js)
                .contains("new URLSearchParams(window.location.search)")
                .contains("window.history.replaceState({}, document.title, window.location.pathname)")
                .contains("fetch(\"/auth/verify-email\"")
                .contains("method: \"POST\"")
                .contains("\"Content-Type\": \"application/json\"")
                .contains("JSON.stringify({ token: rawToken })")
                .doesNotContain("localStorage")
                .doesNotContain("sessionStorage")
                .doesNotContain("document.cookie")
                .doesNotContain("console.log");
    }

    @Test
    void productionConfigShouldUsePortAndVerificationBaseUrlVariable() throws Exception {
        String prodYaml = Files.readString(Path.of("src/main/resources/application-prod.yml"));

        assertThat(prodYaml)
                .contains("port: ${PORT:8080}")
                .contains("address: 0.0.0.0")
                .contains("verification-base-url:")
                .contains("verification-path: /verify-email")
                .doesNotContain("localhost:3000");
    }
}
