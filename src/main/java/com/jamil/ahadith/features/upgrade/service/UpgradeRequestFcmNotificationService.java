package com.jamil.ahadith.features.upgrade.service;

import com.jamil.ahadith.features.notification.fcm.FcmNotificationPayload;
import com.jamil.ahadith.features.notification.fcm.FcmSendResult;
import com.jamil.ahadith.features.notification.fcm.FcmSender;
import com.jamil.ahadith.features.notification.repository.UserFcmTokenRepository;
import com.jamil.ahadith.features.notification.service.UserFcmTokenService;
import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpgradeRequestFcmNotificationService {

    private final UserFcmTokenRepository userFcmTokenRepository;
    private final FcmSender fcmSender;
    private final UserFcmTokenService userFcmTokenService;

    public void sendReviewNotification(
            UUID userId,
            UpgradeStatus status
    ) {
        var tokens =
                userFcmTokenRepository.findDistinctTokensByUserId(
                        userId
                );

        if (tokens.isEmpty()) {
            return;
        }

        String title;
        String body;

        if (status == UpgradeStatus.approved) {
            title = "مبارك لك";
            body = "لقد تم قبول طلب ترقيتك";
        } else if (status == UpgradeStatus.rejected) {
            title = "طلب الترقية";
            body = "تم رفض طلب الترقية، راجع الملاحظات";
        } else {
            return;
        }

        FcmSendResult result = fcmSender.send(
                tokens,
                new FcmNotificationPayload(
                        title,
                        body,
                        Map.of(
                                "type", "upgrade_request",
                                "status", status.name()
                        )
                )
        );

        userFcmTokenService.deleteInvalidTokens(
                result.invalidTokens()
        );
    }
}