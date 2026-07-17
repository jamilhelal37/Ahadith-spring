package com.jamil.ahadith.features.audit.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class ActivityLogResponseDto {
    private UUID id;
    private UUID actorUserId;
    private String actorName;
    private String actorEmail;
    private String actorAvatarUrl;
    private String message;
    private String tableName;
    private UUID recordId;
    private Map<String, Object> oldData;
    private Map<String, Object> newData;
    private LocalDateTime createdAt;
}
