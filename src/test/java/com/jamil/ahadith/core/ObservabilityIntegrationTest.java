package com.jamil.ahadith.core;

import com.jamil.ahadith.features.search.service.SearchFiltersService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ObservabilityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private SearchFiltersService searchFiltersService;

    @Test
    void shouldCorrelateRequestsAndExposePublicHealthOnly() throws Exception {
        mockMvc.perform(get("/books/not-a-uuid").header("X-Request-Id", "test-request-123"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Request-Id", "test-request-123"))
                .andExpect(jsonPath("$.requestId").value("test-request-123"));

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.requestId").exists());

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void filtersListShouldRecordPublicCacheHitAndMissMetrics() throws Exception {
        searchFiltersService.evictReferenceCaches();
        double missesBefore = counter("app.public_cache.requests", "filters-list", "miss");
        double hitsBefore = counter("app.public_cache.requests", "filters-list", "hit");

        mockMvc.perform(get("/api/v1/search/filters"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/search/filters"))
                .andExpect(status().isOk());

        assertThat(counter("app.public_cache.requests", "filters-list", "miss")).isGreaterThan(missesBefore);
        assertThat(counter("app.public_cache.requests", "filters-list", "hit")).isGreaterThan(hitsBefore);
    }

    private double counter(String name, String cache, String result) {
        Counter counter = meterRegistry.find(name)
                .tag("cache", cache)
                .tag("result", result)
                .counter();
        return counter == null ? 0 : counter.count();
    }
}
