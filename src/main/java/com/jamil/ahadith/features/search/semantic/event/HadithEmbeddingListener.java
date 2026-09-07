package com.jamil.ahadith.features.search.semantic.event;

import com.jamil.ahadith.features.search.semantic.service.HadithEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HadithEmbeddingListener {
    private static final Logger log = LoggerFactory.getLogger(HadithEmbeddingListener.class);
    private final HadithEmbeddingService embeddingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHadithTextChanged(HadithTextChangedEvent event) {
        try {
            embeddingService.embedHadith(event.hadithId(), event.text());
        } catch (RuntimeException ex) {
            log.warn("Failed to generate Hadith embedding after commit hadithId={}", event.hadithId(), ex);
        }
    }
}
