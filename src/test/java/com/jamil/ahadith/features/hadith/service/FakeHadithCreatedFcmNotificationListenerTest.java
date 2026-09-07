package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.features.hadith.event.FakeHadithCreatedEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FakeHadithCreatedFcmNotificationListenerTest {

    @Test
    void listenerShouldSwallowRuntimeFailuresFromFirebasePath() {
        FakeHadithCreatedFcmNotificationService service =
                mock(FakeHadithCreatedFcmNotificationService.class);

        FakeHadithCreatedFcmNotificationListener listener =
                new FakeHadithCreatedFcmNotificationListener(service);

        UUID fakeHadithId = UUID.randomUUID();
        String text = "هذا نص حديث تجريبي لا يصح";

        doThrow(new RuntimeException("firebase unavailable"))
                .when(service)
                .sendCreatedNotification(
                        fakeHadithId,
                        text
                );

        assertDoesNotThrow(
                () -> listener.onFakeHadithCreated(
                        new FakeHadithCreatedEvent(
                                fakeHadithId,
                                text
                        )
                )
        );

        verify(service).sendCreatedNotification(
                fakeHadithId,
                text
        );
    }
}