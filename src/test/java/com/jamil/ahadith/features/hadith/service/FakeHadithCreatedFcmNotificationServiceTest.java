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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
        String fakeHadithText =
                "هذا نص حديث منتشر لا يصح";

        when(
                userFcmTokenRepository
                        .findDistinctTokensForActiveUsers()
        ).thenReturn(
                List.of(
                        "token-1",
                        "token-2"
                )
        );

        when(
                fcmSender.send(
                        anyList(),
                        any()
                )
        ).thenReturn(
                new FcmSendResult(
                        1,
                        1,
                        Set.of("token-2")
                )
        );

        service.sendCreatedNotification(
                fakeHadithId,
                fakeHadithText
        );

        ArgumentCaptor<FcmNotificationPayload> payloadCaptor =
                ArgumentCaptor.forClass(
                        FcmNotificationPayload.class
                );

        verify(fcmSender).send(
                eq(
                        List.of(
                                "token-1",
                                "token-2"
                        )
                ),
                payloadCaptor.capture()
        );

        FcmNotificationPayload payload =
                payloadCaptor.getValue();

        assertThat(payload.title())
                .isEqualTo(
                        "انتبه حديث منتشر لا يصح"
                );

        assertThat(payload.body())
                .isEqualTo(fakeHadithText);

        assertThat(payload.data())
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of(
                                "type",
                                "fake_hadith",
                                "fakeHadithId",
                                fakeHadithId.toString()
                        )
                );

        assertThat(payload.data())
                .doesNotContainKey("text");

        assertThat(payload.data().values())
                .allMatch(String.class::isInstance);

        verify(userFcmTokenService)
                .deleteInvalidTokens(
                        Set.of("token-2")
                );
    }

    @Test
    void sendCreatedNotificationShouldUseFallbackBodyWhenTextIsBlank() {
        UUID fakeHadithId = UUID.randomUUID();

        when(
                userFcmTokenRepository
                        .findDistinctTokensForActiveUsers()
        ).thenReturn(
                List.of("token-1")
        );

        when(
                fcmSender.send(
                        anyList(),
                        any()
                )
        ).thenReturn(
                FcmSendResult.empty()
        );

        service.sendCreatedNotification(
                fakeHadithId,
                ""
        );

        ArgumentCaptor<FcmNotificationPayload> payloadCaptor =
                ArgumentCaptor.forClass(
                        FcmNotificationPayload.class
                );

        verify(fcmSender).send(
                eq(List.of("token-1")),
                payloadCaptor.capture()
        );

        FcmNotificationPayload payload =
                payloadCaptor.getValue();

        assertThat(payload.body())
                .isEqualTo(
                        "تمت إضافة حديث منتشر لا يصح."
                );
    }

    @Test
    void sendCreatedNotificationShouldNotCallFirebaseWhenNoTokensExist() {
        when(
                userFcmTokenRepository
                        .findDistinctTokensForActiveUsers()
        ).thenReturn(List.of());

        service.sendCreatedNotification(
                UUID.randomUUID(),
                "حديث تجريبي"
        );

        verify(
                fcmSender,
                never()
        ).send(
                anyList(),
                any()
        );

        verify(
                userFcmTokenService,
                never()
        ).deleteInvalidTokens(any());
    }
}