package com.jamil.ahadith.features.upgrade.service;

import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentStorageException;
import com.jamil.ahadith.features.upgrade.storage.UpgradeDocumentStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UpgradeRequestCleanupListener {
    private static final Logger log = LoggerFactory.getLogger(UpgradeRequestCleanupListener.class);
    private final UpgradeDocumentStorageService documentStorageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDelete(UpgradeRequestDeletedEvent event) {
        if (event.publicId() == null || event.publicId().isBlank()) {
            return;
        }
        try {
            documentStorageService.delete(event.publicId());
        } catch (UpgradeDocumentStorageException ex) {
            log.warn("Failed to delete upgrade document after request deletion publicId: {}", event.publicId(), ex);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleRollbackOnCreate(UpgradeRequestCreatedEvent event) {
        if (event.publicId() == null || event.publicId().isBlank()) {
            return;
        }
        try {
            documentStorageService.delete(event.publicId());
        } catch (UpgradeDocumentStorageException ex) {
            log.warn("Failed to delete uploaded upgrade document after transaction rollback publicId: {}", event.publicId(), ex);
        }
    }
}