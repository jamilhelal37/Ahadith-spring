package com.jamil.ahadith.features.interaction.service;

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
public class QuestionFcmNotificationService {

    private final UserFcmTokenRepository userFcmTokenRepository;
    private final FcmSender fcmSender;
    private final UserFcmTokenService userFcmTokenService;

    public void sendQuestionActivatedNotification(
            UUID userId,
            UUID questionId
    ) {
        var tokens =
                userFcmTokenRepository.findDistinctTokensByUserId(
                        userId
                );

        if (tokens.isEmpty()) {
            return;
        }

        FcmSendResult result =
                fcmSender.send(
                        tokens,
                        new FcmNotificationPayload(
                                "تمت الإجابة على سؤالك من العالم المختص",
                                "شاهد الإجابة",
                                Map.of(
                                        "type", "question",
                                        "questionId", questionId.toString()
                                )
                        )
                );

        userFcmTokenService.deleteInvalidTokens(
                result.invalidTokens()
        );
    }
}