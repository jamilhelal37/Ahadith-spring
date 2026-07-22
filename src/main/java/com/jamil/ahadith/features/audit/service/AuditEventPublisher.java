package com.jamil.ahadith.features.audit.service;

import com.jamil.ahadith.features.audit.event.AuditActorSnapshot;
import com.jamil.ahadith.features.audit.event.AuditEvent;
import com.jamil.ahadith.features.audit.event.AuditOperation;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CurrentUserService currentUserService;

    public void publishCreate(String tableName, UUID recordId, Map<String, Object> newData) {
        publish(AuditOperation.CREATE, tableName, recordId, Map.of(), newData, "created " + tableName);
    }

    public void publishUpdate(String tableName, UUID recordId, Map<String, Object> oldData, Map<String, Object> newData) {
        publish(AuditOperation.UPDATE, tableName, recordId, oldData, newData, "updated " + tableName);
    }

    public void publishDelete(String tableName, UUID recordId, Map<String, Object> oldData) {
        publish(AuditOperation.DELETE, tableName, recordId, oldData, Map.of(), "deleted " + tableName);
    }

    public void publishUpdateAs(User actor, String tableName, UUID recordId, Map<String, Object> oldData,
                                Map<String, Object> newData, String message) {
        publish(new AuditEvent(
                AuditOperation.UPDATE,
                tableName,
                recordId,
                snapshot(actor),
                safe(oldData),
                safe(newData),
                message
        ));
    }

    private void publish(AuditOperation operation, String tableName, UUID recordId, Map<String, Object> oldData,
                         Map<String, Object> newData, String message) {
        currentUserService.getCurrentUser()
                .map(this::snapshot)
                .ifPresent(actor -> publish(new AuditEvent(
                        operation,
                        tableName,
                        recordId,
                        actor,
                        safe(oldData),
                        safe(newData),
                        message
                )));
    }

    private void publish(AuditEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    private AuditActorSnapshot snapshot(User user) {
        return new AuditActorSnapshot(user.getId(), user.getName(), user.getEmail(), user.getAvatarUrl());
    }

    private Map<String, Object> safe(Map<String, Object> value) {
        return value == null ? Map.of() : value;
    }
}
