package com.jamil.ahadith.features.notification.fcm;

import java.util.Map;

public record FcmNotificationPayload(
        String title,
        String body,
        Map<String, String> data
) {
}
