package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    boolean existsByActorEmailAndMessageContainingIgnoreCase(String actorEmail, String message);

    boolean existsByTableName(String tableName);

    @Query("""
            select l from ActivityLog l
            where (:actorUserId is null or l.actorUserId = :actorUserId)
              and (:tableName is null or lower(l.tableName) = lower(:tableName))
              and (:message is null or lower(l.message) like lower(concat('%', :message, '%')))
            """)
    Page<ActivityLog> search(@Param("actorUserId") UUID actorUserId,
                             @Param("tableName") String tableName,
                             @Param("message") String message,
                             Pageable pageable);
}
