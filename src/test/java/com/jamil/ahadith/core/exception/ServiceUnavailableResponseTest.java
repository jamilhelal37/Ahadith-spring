package com.jamil.ahadith.core.exception;

import com.jamil.ahadith.features.hadith.controller.publicapi.PublicHadithController;
import com.jamil.ahadith.features.hadith.service.PublicHadithDetailsService;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceUnavailableResponseTest {
    @Test
    void semanticFailureReturnsTheNormalClean503Response() throws Exception {
        HadithSearchService searchService = mock(HadithSearchService.class);
        when(searchService.publicSearch(any()))
                .thenThrow(new ServiceUnavailableException("Semantic search is temporarily unavailable"));
        PublicHadithController controller = new PublicHadithController(
                searchService, mock(PublicHadithDetailsService.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/v1/ahadith/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"رحمة\",\"mode\":\"SEMANTIC\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("Semantic search is temporarily unavailable"));
    }
}
