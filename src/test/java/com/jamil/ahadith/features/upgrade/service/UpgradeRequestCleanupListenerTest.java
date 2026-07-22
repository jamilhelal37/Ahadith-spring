package com.jamil.ahadith.features.upgrade.service;

import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentStorageException;
import com.jamil.ahadith.features.upgrade.storage.UpgradeDocumentStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpgradeRequestCleanupListenerTest {

    @Mock
    private UpgradeDocumentStorageService documentStorageService;

    @InjectMocks
    private UpgradeRequestCleanupListener listener;

    @Test
    void handleDelete_ShouldDelete_WhenPublicIdIsPresent() {
        UpgradeRequestDeletedEvent event = new UpgradeRequestDeletedEvent("doc-id");
        listener.handleDelete(event);
        verify(documentStorageService).delete("doc-id");
    }

    @Test
    void handleDelete_ShouldNotDelete_WhenPublicIdIsNull() {
        UpgradeRequestDeletedEvent event = new UpgradeRequestDeletedEvent(null);
        listener.handleDelete(event);
        verify(documentStorageService, never()).delete(anyString());
    }

    @Test
    void handleRollbackOnCreate_ShouldDelete_WhenPublicIdIsPresent() {
        UpgradeRequestCreatedEvent event = new UpgradeRequestCreatedEvent(null, "doc-id");
        listener.handleRollbackOnCreate(event);
        verify(documentStorageService).delete("doc-id");
    }

    @Test
    void handleRollbackOnCreate_ShouldNotThrow_WhenStorageFails() {
        UpgradeRequestCreatedEvent event = new UpgradeRequestCreatedEvent(null, "doc-id");
        doThrow(new UpgradeDocumentStorageException("Storage error", null)).when(documentStorageService).delete("doc-id");
        
        listener.handleRollbackOnCreate(event);
        
        verify(documentStorageService).delete("doc-id");
    }
}
