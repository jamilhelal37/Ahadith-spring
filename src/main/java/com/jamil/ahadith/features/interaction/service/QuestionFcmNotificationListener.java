package com.jamil.ahadith.features.interaction.service;

import com.jamil.ahadith.features.interaction.event.QuestionActivatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class QuestionFcmNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    QuestionFcmNotificationListener.class
            );

    private final QuestionFcmNotificationService notificationService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onQuestionActivated(
            QuestionActivatedEvent event
    ) {
        try {
            notificationService
                    .sendQuestionActivatedNotification(
                            event.userId(),
                            event.questionId()
                    );
        } catch (RuntimeException ex) {
            log.warn(
                    "Failed to send question activated FCM notification userId={} questionId={}",
                    event.userId(),
                    event.questionId(),
                    ex
            );
        }
    }
}