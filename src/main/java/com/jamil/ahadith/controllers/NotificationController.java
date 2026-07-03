package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.NotificationRequestDto;
import com.jamil.ahadith.dtos.responses.NotificationResponseDto;
import com.jamil.ahadith.services.NotificationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponseDto> getNotifications() {
        return notificationService.getNotifications();
    }

    @GetMapping("/{id}")
    public NotificationResponseDto getNotificationById(@PathVariable UUID id) {
        return notificationService.getNotificationById(id);
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(@Valid @RequestBody NotificationRequestDto request,
                                                                      UriComponentsBuilder uriBuilder) {
        var notification = notificationService.createNotification(request);
        var uri = uriBuilder.path("/notifications/{id}").buildAndExpand(notification.getId()).toUri();
        return ResponseEntity.created(uri).body(notification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
