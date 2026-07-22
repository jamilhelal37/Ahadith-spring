package com.jamil.ahadith.features.audit.service;

import com.jamil.ahadith.features.audit.entity.ActivityLog;
import com.jamil.ahadith.features.audit.event.AuditEvent;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditEventListener {
    private final ActivityLogRepository activityLogRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAuditEvent(AuditEvent event) {
        ActivityLog log = new ActivityLog();
        log.setActorUserId(event.actor().userId());
        log.setActorName(event.actor().name());
        log.setActorEmail(event.actor().email());
        log.setActorAvatarUrl(event.actor().avatarUrl());
        log.setMessage(event.message());
        log.setTableName(event.tableName());
        log.setRecordId(event.recordId());
        log.setOldData(enrich(event, event.oldData()));
        log.setNewData(enrich(event, event.newData()));
        activityLogRepository.save(log);
    }

    private Map<String, Object> enrich(AuditEvent event, Map<String, Object> data) {
        Map<String, Object> enriched = new LinkedHashMap<>();
        enriched.put("operation", event.operation().name());
        enriched.put("table", event.tableName());
        enriched.putAll(data);
        return enriched;
    }
}
