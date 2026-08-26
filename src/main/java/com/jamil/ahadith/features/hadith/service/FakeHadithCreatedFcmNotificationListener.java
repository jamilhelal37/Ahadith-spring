package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.features.hadith.event.FakeHadithCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FakeHadithCreatedFcmNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(FakeHadithCreatedFcmNotificationListener.class);

    private final FakeHadithCreatedFcmNotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFakeHadithCreated(FakeHadithCreatedEvent event) {
        try {
            notificationService.sendCreatedNotification(event.fakeHadithId());
        } catch (RuntimeException ex) {
            log.warn("Failed to send fake hadith FCM notification fakeHadithId={}", event.fakeHadithId(), ex);
        }
    }
}
