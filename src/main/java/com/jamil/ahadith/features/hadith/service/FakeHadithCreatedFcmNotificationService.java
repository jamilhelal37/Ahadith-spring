package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.features.notification.fcm.FcmNotificationPayload;
import com.jamil.ahadith.features.notification.fcm.FcmSendResult;
import com.jamil.ahadith.features.notification.fcm.FcmSender;
import com.jamil.ahadith.features.notification.repository.UserFcmTokenRepository;
import com.jamil.ahadith.features.notification.service.UserFcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FakeHadithCreatedFcmNotificationService {
    private static final String TITLE = "حديث منتشر لا يصح";
    private static final String BODY = "تمت إضافة حديث جديد، اضغط لعرض التفاصيل.";

    private final UserFcmTokenRepository userFcmTokenRepository;
    private final FcmSender fcmSender;
    private final UserFcmTokenService userFcmTokenService;

    public void sendCreatedNotification(UUID fakeHadithId) {
        var tokens = userFcmTokenRepository.findDistinctTokensForActiveUsers();
        if (tokens.isEmpty()) {
            return;
        }

        FcmSendResult result = fcmSender.send(tokens, new FcmNotificationPayload(
                TITLE,
                BODY,
                Map.of(
                        "type", "fake_hadith",
                        "fakeHadithId", fakeHadithId.toString()
                )
        ));
        userFcmTokenService.deleteInvalidTokens(result.invalidTokens());
    }
}
