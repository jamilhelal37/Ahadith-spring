package com.jamil.ahadith.features.search.service;

import com.jamil.ahadith.core.exception.ServiceUnavailableException;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import com.jamil.ahadith.features.search.dto.request.SearchMode;
import com.jamil.ahadith.features.search.semantic.client.EmbeddingServiceException;
import com.jamil.ahadith.features.search.semantic.service.SemanticSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HadithSearchSemanticTest {
    private HadithRepository repository;
    private SemanticSearchService semantic;
    private HadithSearchService service;

    @BeforeEach
    void setUp() {
        repository = mock(HadithRepository.class);
        semantic = mock(SemanticSearchService.class);
        service = new HadithSearchService(mock(SearchHistoryService.class), repository,
                mock(BookRepository.class), semantic);
    }

    @Test
    void semanticPaginationHappensAfterRanking() {
        List<UUID> ranked = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(semantic.candidateLimit(4)).thenReturn(100);
        when(semantic.semanticCandidates(anyString(), any(), eq(100))).thenReturn(ranked);
        when(repository.findSearchRowsByIdsJpa(anyList())).thenReturn(List.of());
        HadithSearchRequest request = request(SearchMode.SEMANTIC);
        request.setPage(1);
        request.setSize(2);

        var response = service.publicSearch(request);

        assertThat(response.getPagination().getTotalItems()).isEqualTo(3);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(2);
        assertThat(response.getPagination().isHasPrevious()).isTrue();
        verify(repository).findSearchRowsByIdsJpa(List.of(ranked.get(2)));
    }

    @Test
    void hybridFallsBackToNormalFlexiblePageWhenEmbeddingServiceFails() {
        when(semantic.isEnabled()).thenReturn(true);
        when(semantic.candidateLimit(anyInt())).thenReturn(100);
        when(repository.searchPublicIds(anyString(), eq("FLEXIBLE"), anyString(), anyBoolean(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(10), 0));
        when(semantic.semanticCandidatesForHybrid(anyString(), any(), eq(100)))
                .thenThrow(new EmbeddingServiceException("offline"));

        service.publicSearch(request(SearchMode.HYBRID));

        verify(repository, times(2)).searchPublicIds(anyString(), eq("FLEXIBLE"), anyString(), anyBoolean(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void semanticFailureIsExposedAsServiceUnavailable() {
        when(semantic.candidateLimit(anyInt())).thenReturn(100);
        when(semantic.semanticCandidates(anyString(), any(), eq(100)))
                .thenThrow(new ServiceUnavailableException("Semantic search is temporarily unavailable"));

        assertThatThrownBy(() -> service.publicSearch(request(SearchMode.SEMANTIC)))
                .isInstanceOf(ServiceUnavailableException.class);
    }

    private HadithSearchRequest request(SearchMode mode) {
        HadithSearchRequest request = new HadithSearchRequest();
        request.setQuery("رحمة");
        request.setMode(mode);
        return request;
    }
}
