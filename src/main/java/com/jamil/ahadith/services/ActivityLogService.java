package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.responses.ActivityLogResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.mappers.ActivityLogMapper;
import com.jamil.ahadith.repositories.ActivityLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    private final AdminPageService adminPageService;

    public SearchResponse<ActivityLogResponseDto> getActivityLogs(UUID actorUserId, String tableName, String message,
                                                                  Pageable pageable) {
        return adminPageService.response(activityLogRepository.search(actorUserId, tableName, message, pageable)
                .map(activityLogMapper::toResponseDto));
    }
}
