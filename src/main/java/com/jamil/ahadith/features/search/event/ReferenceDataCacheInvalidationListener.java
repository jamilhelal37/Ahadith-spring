package com.jamil.ahadith.features.search.event;

import com.jamil.ahadith.features.search.service.SearchFiltersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReferenceDataCacheInvalidationListener {
    private final SearchFiltersService searchFiltersService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReferenceDataChanged(ReferenceDataChangedEvent event) {
        searchFiltersService.evictReferenceCaches();
    }
}
