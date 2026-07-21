package com.jamil.ahadith.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.cors.allowed-origins=https://jamilhelal.me,https://www.jamilhelal.me,http://localhost:5173"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorsProperties corsProperties;

    @Test
    void allowedOriginShouldReceiveAllowOriginHeader() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .header(HttpHeaders.ORIGIN, "https://jamilhelal.me"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://jamilhelal.me"));
    }

    @Test
    void disallowedOriginShouldBeRejectedForPreflight() throws Exception {
        mockMvc.perform(options("/ahadith/search")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchPreflightShouldAllowAuthorizationHeader() throws Exception {
        mockMvc.perform(options("/ahadith/search")
                        .header(HttpHeaders.ORIGIN, "https://jamilhelal.me")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://jamilhelal.me"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type"));
    }

    @Test
    void configurationShouldNotUseWildcardOrigins() {
        assertThat(corsProperties.getAllowedOrigins()).doesNotContain("*");
        assertThat(corsProperties.isAllowCredentials()).isFalse();
    }
}
