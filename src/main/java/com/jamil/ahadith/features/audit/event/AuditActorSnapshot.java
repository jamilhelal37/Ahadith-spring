package com.jamil.ahadith.features.audit.event;

import java.util.UUID;

public record AuditActorSnapshot(
        UUID userId,
        String name,
        String email,
        String avatarUrl
) {
}
