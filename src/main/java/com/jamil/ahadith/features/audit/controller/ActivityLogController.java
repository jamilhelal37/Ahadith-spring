package com.jamil.ahadith.features.audit.controller;

import com.jamil.ahadith.features.audit.dto.response.ActivityLogResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.audit.service.ActivityLogService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/admin/activity-logs", "/api/v1/admin/activity-logs"})
public class ActivityLogController {
    private final ActivityLogService activityLogService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<ActivityLogResponseDto> getActivityLogs(
            @RequestParam(required = false) UUID actorUserId,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String message,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return activityLogService.getActivityLogs(actorUserId, tableName, message,
                adminPageService.pageable(page, size, sort,
                        Set.of("createdAt", "id"),
                        Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }
}
