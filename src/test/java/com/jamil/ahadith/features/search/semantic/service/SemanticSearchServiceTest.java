package com.jamil.ahadith.features.search.semantic.service;

import com.jamil.ahadith.features.search.semantic.client.EmbeddingClient;
import com.jamil.ahadith.features.search.semantic.config.SemanticSearchProperties;
import com.jamil.ahadith.features.search.semantic.repository.HadithEmbeddingRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SemanticSearchServiceTest {
    private final SemanticSearchProperties properties = new SemanticSearchProperties();
    private final SemanticSearchService service;

    SemanticSearchServiceTest() {
        properties.setHybridRrfK(60);
        service = new SemanticSearchService(mock(EmbeddingClient.class),
                mock(HadithEmbeddingRepository.class), properties);
    }

    @Test
    void rrfRewardsDocumentsPresentInBothRankingsAndRemovesDuplicates() {
        UUID textOnly = UUID.randomUUID();
        UUID shared = UUID.randomUUID();
        UUID semanticOnly = UUID.randomUUID();

        List<UUID> fused = service.fuse(List.of(textOnly, shared), List.of(semanticOnly, shared));

        assertThat(fused).containsExactly(shared, textOnly, semanticOnly);
    }

    @Test
    void rrfUsesDeterministicUuidTieBreaker() {
        UUID lower = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID higher = UUID.fromString("00000000-0000-0000-0000-000000000002");

        assertThat(service.fuse(List.of(higher), List.of(lower))).containsExactly(lower, higher);
    }
}
