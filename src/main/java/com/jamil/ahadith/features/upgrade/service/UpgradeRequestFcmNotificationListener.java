package com.jamil.ahadith.features.upgrade.service;

import com.jamil.ahadith.features.upgrade.event.UpgradeRequestReviewedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UpgradeRequestFcmNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    UpgradeRequestFcmNotificationListener.class
            );

    private final UpgradeRequestFcmNotificationService notificationService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onUpgradeRequestReviewed(
            UpgradeRequestReviewedEvent event
    ) {
        try {
            notificationService.sendReviewNotification(
                    event.userId(),
                    event.status()
            );
        } catch (RuntimeException ex) {
            log.warn(
                    "Failed to send upgrade request FCM notification userId={} status={}",
                    event.userId(),
                    event.status(),
                    ex
            );
        }
    }
}