package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.responses.ActivityLogResponseDto;
import com.jamil.ahadith.mappers.ActivityLogMapper;
import com.jamil.ahadith.repositories.ActivityLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
@AllArgsConstructor
@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    public List<ActivityLogResponseDto> getActivityLogs(UUID actorUserId, String tableName, String message) {
        return activityLogRepository.findAll().stream()
                .filter(log -> actorUserId == null || actorUserId.equals(log.getActorUserId()))
                .filter(log -> tableName == null || tableName.equalsIgnoreCase(log.getTableName()))
                .filter(log -> message == null || (log.getMessage() != null && log.getMessage().toLowerCase().contains(message.toLowerCase())))
                .map(activityLogMapper::toResponseDto)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }
}
