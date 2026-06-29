package com.jamil.ahadith.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_name")
    private String actorName;

    @Column(name = "actor_email")
    private String actorEmail;

    @Column(name = "actor_avatar_url")
    private String actorAvatarUrl;

    @Column(name = "message")
    private String message;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "record_id")
    private UUID recordId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_data")
    private Map<String, Object> oldData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data")
    private Map<String, Object> newData;

    @Column(name = "created_at",insertable = false, updatable = false)
    private LocalDateTime createdAt;
}