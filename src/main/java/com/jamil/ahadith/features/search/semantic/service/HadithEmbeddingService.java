package com.jamil.ahadith.features.search.semantic.service;

import com.jamil.ahadith.features.search.semantic.client.EmbeddingClient;
import com.jamil.ahadith.features.search.semantic.client.EmbeddingResponse;
import com.jamil.ahadith.features.search.semantic.config.SemanticSearchProperties;
import com.jamil.ahadith.features.search.semantic.repository.EmbeddingStatus;
import com.jamil.ahadith.features.search.semantic.repository.HadithEmbeddingRepository;
import com.jamil.ahadith.features.search.semantic.repository.HadithEmbeddingState;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HadithEmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(HadithEmbeddingService.class);

    private final HadithEmbeddingRepository repository;
    private final EmbeddingClient client;
    private final ContentHashService hashes;
    private final SemanticSearchProperties properties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void embedHadith(UUID hadithId, String exactText) {
        if (!properties.isEnabled()) return;
        if (!exactText.equals(repository.findHadithText(hadithId))) return;
        String contentHash = hashes.sha256(exactText);
        if (repository.isCurrent(hadithId, contentHash, properties.getModel(), properties.getModelVersion())) return;
        EmbeddingResponse response = client.embed(List.of(exactText));
        if (!exactText.equals(repository.findHadithText(hadithId))) return;
        repository.upsert(hadithId, response.embeddings().getFirst(), response.model(),
                response.modelVersion(), contentHash);
    }

    public ReindexResult reindex(boolean force) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("Semantic search is disabled");
        }
        long embedded = 0;
        long failed = 0;
        UUID afterId = null;
        while (true) {
            List<HadithEmbeddingState> batch = repository.findNeedingEmbedding(
                    properties.getModel(), properties.getModelVersion(), force, afterId, properties.getBatchSize());
            if (batch.isEmpty()) break;
            afterId = batch.getLast().hadithId();
            try {
                EmbeddingResponse response = client.embed(batch.stream().map(HadithEmbeddingState::text).toList());
                for (int i = 0; i < batch.size(); i++) {
                    HadithEmbeddingState hadith = batch.get(i);
                    repository.upsert(hadith.hadithId(), response.embeddings().get(i), response.model(),
                            response.modelVersion(), hashes.sha256(hadith.text()));
                    embedded++;
                }
            } catch (RuntimeException ex) {
                failed += batch.size();
                log.warn("Embedding reindex batch failed firstHadithId={} batchSize={}",
                        batch.getFirst().hadithId(), batch.size(), ex);
            }
        }
        return new ReindexResult(embedded, failed, force);
    }

    public EmbeddingStatus status() {
        return repository.status(properties.getModel(), properties.getModelVersion());
    }
}
