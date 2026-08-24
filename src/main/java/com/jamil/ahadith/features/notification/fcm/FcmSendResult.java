package com.jamil.ahadith.features.notification.fcm;

import java.util.Set;

public record FcmSendResult(
        int successCount,
        int failureCount,
        Set<String> invalidTokens
) {
    public static FcmSendResult empty() {
        return new FcmSendResult(0, 0, Set.of());
    }
}
