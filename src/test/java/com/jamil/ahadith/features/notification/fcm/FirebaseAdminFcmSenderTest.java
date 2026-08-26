package com.jamil.ahadith.features.notification.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebaseAdminFcmSenderTest {

    @Test
    void sendShouldSplitTokensIntoFirebaseSafeBatches() throws Exception {
        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
        BatchResponse firstBatch = batchResponse(500, 0, List.of());
        BatchResponse secondBatch = batchResponse(1, 0, List.of());
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class)))
                .thenReturn(firstBatch, secondBatch);
        FirebaseAdminFcmSender sender = new FirebaseAdminFcmSender(firebaseMessaging);

        FcmSendResult result = sender.send(tokens(501), payload());

        assertThat(result.successCount()).isEqualTo(501);
        assertThat(result.failureCount()).isZero();

        ArgumentCaptor<MulticastMessage> messageCaptor = ArgumentCaptor.forClass(MulticastMessage.class);
        verify(firebaseMessaging, org.mockito.Mockito.times(2)).sendEachForMulticast(messageCaptor.capture());
        assertThat(tokensIn(messageCaptor.getAllValues().get(0))).hasSize(500);
        assertThat(tokensIn(messageCaptor.getAllValues().get(1))).hasSize(1);
    }

    @Test
    void sendShouldCollectInvalidTokensFromPartialFailures() throws Exception {
        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
        SendResponse failed = mock(SendResponse.class);
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(failed.isSuccessful()).thenReturn(false);
        when(failed.getException()).thenReturn(exception);
        when(exception.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
        BatchResponse batchResponse = batchResponse(1, 1, List.of(successfulResponse(), failed));
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class)))
                .thenReturn(batchResponse);
        FirebaseAdminFcmSender sender = new FirebaseAdminFcmSender(firebaseMessaging);

        FcmSendResult result = sender.send(List.of("valid-token", "invalid-token"), payload());

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(1);
        assertThat(result.invalidTokens()).containsExactly("invalid-token");
    }

    @Test
    void sendShouldReturnEmptyResultAndNotCallFirebaseWhenTokensAreEmpty() throws Exception {
        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
        FirebaseAdminFcmSender sender = new FirebaseAdminFcmSender(firebaseMessaging);

        FcmSendResult result = sender.send(List.of(), payload());

        assertThat(result).isEqualTo(FcmSendResult.empty());
        verify(firebaseMessaging, never()).sendEachForMulticast(any(MulticastMessage.class));
    }

    @Test
    void sendShouldHandleIllegalArgumentExceptionWithoutThrowing() throws Exception {
        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
        FirebaseAdminFcmSender sender = new FirebaseAdminFcmSender(firebaseMessaging);

        FcmSendResult result = sender.send(List.of(""), payload());

        assertThat(result.successCount()).isZero();
        assertThat(result.failureCount()).isEqualTo(1);
        assertThat(result.invalidTokens()).isEmpty();
        verify(firebaseMessaging, never()).sendEachForMulticast(any(MulticastMessage.class));
    }

    @Test
    void sendShouldHandleFirebaseMessagingExceptionWithoutThrowing() throws Exception {
        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenThrow(exception);
        FirebaseAdminFcmSender sender = new FirebaseAdminFcmSender(firebaseMessaging);

        FcmSendResult result = sender.send(List.of("token-1", "token-2"), payload());

        assertThat(result.successCount()).isZero();
        assertThat(result.failureCount()).isEqualTo(2);
        assertThat(result.invalidTokens()).isEmpty();
    }

    private FcmNotificationPayload payload() {
        return new FcmNotificationPayload("title", "body", Map.of("type", "fake_hadith"));
    }

    private List<String> tokens(int count) {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tokens.add("token-" + i);
        }
        return tokens;
    }

    private BatchResponse batchResponse(int successCount, int failureCount, List<SendResponse> responses) {
        BatchResponse response = mock(BatchResponse.class);
        when(response.getSuccessCount()).thenReturn(successCount);
        when(response.getFailureCount()).thenReturn(failureCount);
        when(response.getResponses()).thenReturn(responses);
        return response;
    }

    private SendResponse successfulResponse() {
        SendResponse response = mock(SendResponse.class);
        when(response.isSuccessful()).thenReturn(true);
        return response;
    }

    @SuppressWarnings("unchecked")
    private List<String> tokensIn(MulticastMessage message) throws Exception {
        Field field = MulticastMessage.class.getDeclaredField("tokens");
        field.setAccessible(true);
        return (List<String>) field.get(message);
    }
}
