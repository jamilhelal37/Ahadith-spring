package com.jamil.ahadith.features.audit.event;

import java.util.Map;
import java.util.UUID;

public record AuditEvent(
        AuditOperation operation,
        String tableName,
        UUID recordId,
        AuditActorSnapshot actor,
        Map<String, Object> oldData,
        Map<String, Object> newData,
        String message
) {
}
