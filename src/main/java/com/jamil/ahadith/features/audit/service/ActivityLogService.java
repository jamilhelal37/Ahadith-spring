package com.jamil.ahadith.features.audit.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.audit.dto.response.ActivityLogResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.audit.mapper.ActivityLogMapper;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.audit.repository.ActivityLogSpecifications;
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
        return adminPageService.response(activityLogRepository.findAll(
                        ActivityLogSpecifications.search(actorUserId, tableName, message), pageable)
                .map(activityLogMapper::toResponseDto));
    }
}
