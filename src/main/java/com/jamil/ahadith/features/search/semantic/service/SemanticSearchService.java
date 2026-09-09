package com.jamil.ahadith.features.search.semantic.service;

import com.jamil.ahadith.core.exception.ServiceUnavailableException;
import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import com.jamil.ahadith.features.search.semantic.client.EmbeddingClient;
import com.jamil.ahadith.features.search.semantic.client.EmbeddingServiceException;
import com.jamil.ahadith.features.search.semantic.config.SemanticSearchProperties;
import com.jamil.ahadith.features.search.semantic.repository.HadithEmbeddingRepository;
import com.jamil.ahadith.features.search.semantic.repository.SemanticCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SemanticSearchService {
    private final EmbeddingClient client;
    private final HadithEmbeddingRepository repository;
    private final SemanticSearchProperties properties;

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public int candidateLimit(int required) {
        int cap = Math.max(1, properties.getMaxCandidateLimit());
        int baseline = Math.min(Math.max(1, properties.getCandidateLimit()), cap);
        return Math.min(Math.max(required, baseline), cap);
    }

    public List<UUID> semanticCandidates(String originalQuery, HadithSearchRequest request, int limit) {
        requireEnabled();
        try {
            var response = client.embed(List.of(originalQuery));
            return repository.search(response.embeddings().getFirst(), request,
                            properties.getMinSimilarity(), limit)
                    .stream().map(SemanticCandidate::hadithId).toList();
        } catch (EmbeddingServiceException ex) {
            throw new ServiceUnavailableException("Semantic search is temporarily unavailable");
        }
    }

    public List<UUID> semanticCandidatesForHybrid(String originalQuery, HadithSearchRequest request, int limit) {
        var response = client.embed(List.of(originalQuery));
        return repository.search(response.embeddings().getFirst(), request,
                        properties.getMinSimilarity(), limit)
                .stream().map(SemanticCandidate::hadithId).toList();
    }

    public List<UUID> fuse(List<UUID> textIds, List<UUID> semanticIds) {
        Map<UUID, Double> scores = new HashMap<>();
        addScores(scores, textIds);
        addScores(scores, semanticIds);
        LinkedHashSet<UUID> allIds = new LinkedHashSet<>();
        allIds.addAll(textIds);
        allIds.addAll(semanticIds);
        List<UUID> fused = new ArrayList<>(allIds);
        fused.sort(Comparator
                .comparingDouble((UUID id) -> scores.getOrDefault(id, 0.0)).reversed()
                .thenComparing(UUID::toString));
        return fused;
    }

    private void addScores(Map<UUID, Double> scores, List<UUID> ids) {
        for (int index = 0; index < ids.size(); index++) {
            int oneBasedRank = index + 1;
            scores.merge(ids.get(index), 1.0 / (properties.getHybridRrfK() + oneBasedRank), Double::sum);
        }
    }

    private void requireEnabled() {
        if (!properties.isEnabled()) {
            throw new ServiceUnavailableException("Semantic search is disabled");
        }
    }
}
