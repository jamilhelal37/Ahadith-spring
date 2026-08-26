package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.features.notification.fcm.FcmNotificationPayload;
import com.jamil.ahadith.features.notification.fcm.FcmSendResult;
import com.jamil.ahadith.features.notification.fcm.FcmSender;
import com.jamil.ahadith.features.notification.repository.UserFcmTokenRepository;
import com.jamil.ahadith.features.notification.service.UserFcmTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FakeHadithCreatedFcmNotificationServiceTest {
    @Mock
    private UserFcmTokenRepository userFcmTokenRepository;
    @Mock
    private FcmSender fcmSender;
    @Mock
    private UserFcmTokenService userFcmTokenService;

    @InjectMocks
    private FakeHadithCreatedFcmNotificationService service;

    @Test
    void sendCreatedNotificationShouldSendExpectedPayloadAndDeleteInvalidTokens() {
        UUID fakeHadithId = UUID.randomUUID();
        when(userFcmTokenRepository.findDistinctTokensForActiveUsers()).thenReturn(List.of("token-1", "token-2"));
        when(fcmSender.send(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new FcmSendResult(1, 1, Set.of("token-2")));

        service.sendCreatedNotification(fakeHadithId);

        ArgumentCaptor<FcmNotificationPayload> payloadCaptor = ArgumentCaptor.forClass(FcmNotificationPayload.class);
        verify(fcmSender).send(org.mockito.ArgumentMatchers.eq(List.of("token-1", "token-2")),
                payloadCaptor.capture());
        FcmNotificationPayload payload = payloadCaptor.getValue();
        assertThat(payload.title()).isEqualTo("حديث منتشر لا يصح");
        assertThat(payload.body()).isEqualTo("تمت إضافة حديث جديد، اضغط لعرض التفاصيل.");
        assertThat(payload.data()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                "type", "fake_hadith",
                "fakeHadithId", fakeHadithId.toString()
        ));
        assertThat(payload.data()).doesNotContainKey("text");
        assertThat(payload.data().values()).allMatch(String.class::isInstance);
        verify(userFcmTokenService).deleteInvalidTokens(Set.of("token-2"));
    }

    @Test
    void sendCreatedNotificationShouldNotCallFirebaseWhenNoTokensExist() {
        when(userFcmTokenRepository.findDistinctTokensForActiveUsers()).thenReturn(List.of());

        service.sendCreatedNotification(UUID.randomUUID());

        verify(fcmSender, never()).send(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.any());
        verify(userFcmTokenService, never()).deleteInvalidTokens(org.mockito.ArgumentMatchers.any());
    }
}
