package com.jamil.ahadith.features.notification.controller;

import com.jamil.ahadith.features.notification.entity.Notification;

import com.jamil.ahadith.features.notification.dto.request.NotificationRequestDto;
import com.jamil.ahadith.features.notification.dto.response.NotificationResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<NotificationResponseDto> getNotifications(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "20") int size,
                                                                    @RequestParam(required = false) String sort) {
        return notificationService.getNotifications(adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "updatedAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @GetMapping("/{id}")
    public NotificationResponseDto getNotificationById(@PathVariable UUID id) {
        return notificationService.getNotificationById(id);
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(@Valid @RequestBody NotificationRequestDto request,
                                                                      UriComponentsBuilder uriBuilder) {
        var notification = notificationService.createNotification(request);
        var uri = uriBuilder.path("/admin/notifications/{id}").buildAndExpand(notification.getId()).toUri();
        return ResponseEntity.created(uri).body(notification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
