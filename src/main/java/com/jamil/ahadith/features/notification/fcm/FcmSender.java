package com.jamil.ahadith.features.notification.fcm;

import java.util.List;

public interface FcmSender {
    FcmSendResult send(List<String> tokens, FcmNotificationPayload payload);
}
