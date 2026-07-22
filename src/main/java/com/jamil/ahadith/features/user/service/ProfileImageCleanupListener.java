package com.jamil.ahadith.features.user.service;

import com.jamil.ahadith.core.storage.CloudinaryStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProfileImageCleanupListener {
    private static final Logger log = LoggerFactory.getLogger(ProfileImageCleanupListener.class);

    private final CloudinaryStorageService cloudinaryStorageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteOldImage(ProfileImageChangedEvent event) {
        if (event.oldPublicId() == null || event.oldPublicId().isBlank()) {
            return;
        }

        if (event.oldPublicId().equals(event.newPublicId())) {
            log.info("Skipping image deletion because oldPublicId equals newPublicId: {}", event.oldPublicId());
            return;
        }

        try {
            cloudinaryStorageService.deleteImage(event.oldPublicId());
        } catch (RuntimeException ex) {
            log.warn("Failed to delete replaced profile image with publicId: {}", event.oldPublicId(), ex);
        }
    }
}
