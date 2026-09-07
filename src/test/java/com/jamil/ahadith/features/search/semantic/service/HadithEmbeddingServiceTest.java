package com.jamil.ahadith.features.search.semantic.service;

import com.jamil.ahadith.features.search.semantic.client.EmbeddingClient;
import com.jamil.ahadith.features.search.semantic.client.EmbeddingResponse;
import com.jamil.ahadith.features.search.semantic.config.SemanticSearchProperties;
import com.jamil.ahadith.features.search.semantic.repository.HadithEmbeddingRepository;
import com.jamil.ahadith.features.search.semantic.repository.HadithEmbeddingState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HadithEmbeddingServiceTest {
    private HadithEmbeddingRepository repository;
    private EmbeddingClient client;
    private SemanticSearchProperties properties;
    private HadithEmbeddingService service;

    @BeforeEach
    void setUp() {
        repository = mock(HadithEmbeddingRepository.class);
        client = mock(EmbeddingClient.class);
        properties = new SemanticSearchProperties();
        properties.setEnabled(true);
        properties.setBatchSize(16);
        service = new HadithEmbeddingService(repository, client, new ContentHashService(), properties);
    }

    @Test
    void currentEmbeddingIsSkipped() {
        UUID id = UUID.randomUUID();
        when(repository.findHadithText(id)).thenReturn("text");
        when(repository.isCurrent(eq(id), anyString(), eq("BAAI/bge-m3"), eq("1.3.5"))).thenReturn(true);

        service.embedHadith(id, "text");

        verifyNoInteractions(client);
        verify(repository, never()).upsert(any(), anyList(), anyString(), anyString(), anyString());
    }

    @Test
    void reindexRepairsMissingOrStaleEmbeddingsInABatch() {
        UUID id = UUID.randomUUID();
        when(repository.findNeedingEmbedding(anyString(), anyString(), eq(false), isNull(), eq(16)))
                .thenReturn(List.of(new HadithEmbeddingState(id, "exact text")));
        when(repository.findNeedingEmbedding(anyString(), anyString(), eq(false), eq(id), eq(16)))
                .thenReturn(List.of());
        List<Double> vector = java.util.Collections.nCopies(1024, 0.25);
        when(client.embed(List.of("exact text")))
                .thenReturn(new EmbeddingResponse("BAAI/bge-m3", "1.3.5", 1024, List.of(vector)));

        ReindexResult result = service.reindex(false);

        assertThat(result.embedded()).isEqualTo(1);
        assertThat(result.failed()).isZero();
        verify(repository).upsert(eq(id), eq(vector), eq("BAAI/bge-m3"), eq("1.3.5"), anyString());
    }
}
