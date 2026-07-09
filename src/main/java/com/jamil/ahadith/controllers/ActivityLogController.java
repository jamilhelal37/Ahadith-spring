package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.responses.ActivityLogResponseDto;
import com.jamil.ahadith.services.ActivityLogService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/activity-logs")
public class ActivityLogController {
    private final ActivityLogService activityLogService;

    @GetMapping
    public List<ActivityLogResponseDto> getActivityLogs(
            @RequestParam(required = false) UUID actorUserId,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String message) {
        return activityLogService.getActivityLogs(actorUserId, tableName, message);
    }
}
