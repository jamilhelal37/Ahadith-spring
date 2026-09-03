package com.jamil.ahadith.features.notification.fcm;

import java.util.List;

public class NoOpFcmSender implements FcmSender {
    @Override
    public FcmSendResult send(List<String> tokens, FcmNotificationPayload payload) {
        return FcmSendResult.empty();
    }
}
