package com.jamil.ahadith.features.notification.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FirebaseAdminFcmSender implements FcmSender {
    private static final Logger log = LoggerFactory.getLogger(FirebaseAdminFcmSender.class);
    private static final int MAX_BATCH_SIZE = 500;

    private final FirebaseMessaging firebaseMessaging;

    public FirebaseAdminFcmSender(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public FcmSendResult send(List<String> tokens, FcmNotificationPayload payload) {
        if (tokens == null || tokens.isEmpty()) {
            return FcmSendResult.empty();
        }

        int successCount = 0;
        int failureCount = 0;
        Set<String> invalidTokens = new LinkedHashSet<>();

        for (int start = 0; start < tokens.size(); start += MAX_BATCH_SIZE) {
            List<String> batch = tokens.subList(start, Math.min(start + MAX_BATCH_SIZE, tokens.size()));
            FcmSendResult batchResult = sendBatch(batch, payload);
            successCount += batchResult.successCount();
            failureCount += batchResult.failureCount();
            invalidTokens.addAll(batchResult.invalidTokens());
        }

        return new FcmSendResult(successCount, failureCount, invalidTokens);
    }

    private FcmSendResult sendBatch(List<String> tokens, FcmNotificationPayload payload) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(payload.title())
                            .setBody(payload.body())
                            .build())
                    .putAllData(payload.data())
                    .addAllTokens(tokens)
                    .build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            FcmSendResult result = toSendResult(tokens, response);
            log.info("FCM multicast batch sent successCount={} failureCount={}",
                    result.successCount(), result.failureCount());
            return result;
        } catch (FirebaseMessagingException | IllegalArgumentException ex) {
            log.warn("FCM multicast batch failed tokenCount={} successCount=0 failureCount={}",
                    tokens.size(), tokens.size(), ex);
            return new FcmSendResult(0, tokens.size(), Set.of());
        }
    }

    private FcmSendResult toSendResult(List<String> tokens, BatchResponse response) {
        Set<String> invalidTokens = new LinkedHashSet<>();
        List<SendResponse> responses = response.getResponses();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful()) {
                continue;
            }
            FirebaseMessagingException exception = sendResponse.getException();
            if (exception != null && isInvalidRegistrationToken(exception.getMessagingErrorCode())) {
                invalidTokens.add(tokens.get(i));
            }
        }

        return new FcmSendResult(
                response.getSuccessCount(),
                response.getFailureCount(),
                invalidTokens
        );
    }

    private boolean isInvalidRegistrationToken(MessagingErrorCode errorCode) {
        return errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
