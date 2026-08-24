package com.jamil.ahadith.features.notification.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FirebaseAdminFcmSender implements FcmSender {
    private final FirebaseMessaging firebaseMessaging;

    public FirebaseAdminFcmSender(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public FcmSendResult send(List<String> tokens, FcmNotificationPayload payload) {
        if (tokens == null || tokens.isEmpty()) {
            return FcmSendResult.empty();
        }

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(payload.title())
                        .setBody(payload.body())
                        .build())
                .putAllData(payload.data())
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            return toSendResult(tokens, response);
        } catch (FirebaseMessagingException ex) {
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
