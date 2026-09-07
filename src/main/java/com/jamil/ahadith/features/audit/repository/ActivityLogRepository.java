package com.jamil.ahadith.features.audit.repository;

import com.jamil.ahadith.features.audit.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID>, JpaSpecificationExecutor<ActivityLog> {
    boolean existsByActorEmailAndMessageContainingIgnoreCase(String actorEmail, String message);

    boolean existsByTableName(String tableName);

}
