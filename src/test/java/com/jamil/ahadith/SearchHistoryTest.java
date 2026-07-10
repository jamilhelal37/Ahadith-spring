package com.jamil.ahadith;

import com.jamil.ahadith.entities.SearchSource;
import com.jamil.ahadith.repositories.SearchHistoryRepository;
import com.jamil.ahadith.repositories.UserRepository;
import com.jamil.ahadith.services.SearchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SearchHistoryTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        searchHistoryRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void searchShouldPersistHistoryForAuthenticatedUser() {
        var user = new com.jamil.ahadith.entities.User();
        user.setId(UUID.randomUUID());
        user.setName("Admin User");
        user.setEmail("admin@example.com");
        user.setPassword("encoded-password");
        userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        searchService.searchHadiths("صحيح", null, null, null, null, null, null, null, 0, 10);

        var history = searchHistoryRepository.findAll();
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getSearchText()).contains("صحيح");
        assertThat(history.get(0).getSearchSource()).isEqualTo(SearchSource.Hadith);
        assertThat(history.get(0).getUser()).isNotNull();
        assertThat(history.get(0).getUser().getEmail()).isEqualTo("admin@example.com");
    }
}
