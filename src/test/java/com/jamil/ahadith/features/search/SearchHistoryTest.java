package com.jamil.ahadith.features.search;

import com.jamil.ahadith.core.web.dto.PaginationMeta;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.features.search.entity.SearchSource;
import com.jamil.ahadith.features.search.repository.SearchHistoryRepository;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SearchHistoryTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HadithSearchService hadithSearchService;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        searchHistoryRepository.deleteAll();
        when(hadithSearchService.publicSearch(any()))
                .thenReturn(new SearchResponse<>(List.<HadithSearchItemDto>of(), new PaginationMeta(0, 20, 0, 0, false, false)));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void modernSearchShouldPersistHistoryForAuthenticatedUser() throws Exception {
        RequestPostProcessor authenticatedUser = authenticate(createUser("admin@example.com"));

        mockMvc.perform(post("/api/v1/ahadith/search")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"sahih\",\"bookIds\":[\"00000000-0000-0000-0000-000000000001\"],\"includeExplanation\":true}"))
                .andExpect(status().isOk());

        var history = searchHistoryRepository.findAll();
        assertThat(history).hasSize(1);
        assertThat(history.getFirst().getSearchText()).contains("sahih");
        assertThat(history.getFirst().getSearchText()).contains("00000000-0000-0000-0000-000000000001");
        assertThat(history.getFirst().getSearchText()).contains("includeExplanation=true");
        assertThat(history.getFirst().getSearchSource()).isEqualTo(SearchSource.Hadith);
        assertThat(history.getFirst().getUser().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void modernSearchShouldNotPersistHistoryForAnonymousVisitor() throws Exception {
        mockMvc.perform(post("/api/v1/ahadith/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"anonymous\"}"))
                .andExpect(status().isOk());

        assertThat(searchHistoryRepository.findAll()).isEmpty();
    }

    @Test
    void searchHistoryShouldBeOwnedSearchableAndDeletable() throws Exception {
        User owner = createUser("owner@example.com");
        User other = createUser("other@example.com");
        RequestPostProcessor ownerAuthentication = authenticate(owner);
        RequestPostProcessor otherAuthentication = authenticate(other);
        mockMvc.perform(post("/api/v1/ahadith/search")
                        .with(ownerAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"owned keyword\"}"))
                .andExpect(status().isOk());
        var ownedItem = searchHistoryRepository.findAll().getFirst();

        mockMvc.perform(post("/api/v1/ahadith/search")
                        .with(otherAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"other keyword\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/me/search-history/search")
                        .with(ownerAuthentication)
                        .param("keyword", "owned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].searchText").value("owned keyword"))
                .andExpect(jsonPath("$[1]").doesNotExist());

        mockMvc.perform(delete("/api/v1/me/search-history/{id}", ownedItem.getId())
                        .with(ownerAuthentication))
                .andExpect(status().isNoContent());
        assertThat(searchHistoryRepository.findByUserOrderByCreatedAtDesc(owner)).isEmpty();
        assertThat(searchHistoryRepository.findByUserOrderByCreatedAtDesc(other)).hasSize(1);

        mockMvc.perform(delete("/api/v1/me/search-history")
                        .with(ownerAuthentication))
                .andExpect(status().isNoContent());
        assertThat(searchHistoryRepository.findByUserOrderByCreatedAtDesc(owner)).isEmpty();
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setStatus(UserStatus.active);
        user.setType(UserType.member);
        return userRepository.saveAndFlush(user);
    }

    private RequestPostProcessor authenticate(User user) {
        return authentication(UsernamePasswordAuthenticationToken.authenticated(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))));
    }
}
