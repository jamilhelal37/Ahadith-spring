package com.jamil.ahadith.features.auth;

import com.jamil.ahadith.features.auth.google.GoogleIdentity;
import com.jamil.ahadith.features.auth.google.GoogleIdentityVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.google-auth.enabled=true",
        "app.google-auth.client-ids=test-web-client-id",
        "app.rate-limit.enabled=true",
        "app.rate-limit.policies.google-login.capacity=1",
        "app.rate-limit.policies.google-login.window=15m"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleLoginRateLimitingIntegrationTest {
    private static final String TOKEN = "rate-google-token";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void googleLoginShouldUseIndependentIpRateLimit() throws Exception {
        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleJson(TOKEN)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleJson(TOKEN)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"));
    }

    private String googleJson(String idToken) {
        return "{\"idToken\":\"" + idToken + "\"}";
    }

    @TestConfiguration
    static class GoogleRateLimitTestConfig {
        @Bean
        @Primary
        GoogleIdentityVerifier googleIdentityVerifier() {
            return new RateLimitFakeGoogleIdentityVerifier();
        }
    }

    static class RateLimitFakeGoogleIdentityVerifier implements GoogleIdentityVerifier {
        private final Map<String, GoogleIdentity> identities = new ConcurrentHashMap<>();

        RateLimitFakeGoogleIdentityVerifier() {
            identities.put(TOKEN, new GoogleIdentity(
                    "rate-google-sub",
                    "rate-google@example.com",
                    true,
                    "Rate Google",
                    null
            ));
        }

        @Override
        public GoogleIdentity verify(String idToken) {
            return identities.get(idToken);
        }
    }
}
