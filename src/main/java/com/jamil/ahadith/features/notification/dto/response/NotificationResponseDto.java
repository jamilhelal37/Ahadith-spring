package com.jamil.ahadith.features.notification.dto.response;

import com.jamil.ahadith.features.notification.entity.Notification;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;

import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponseDto {
    private UUID id;
    private String title;
    private String body;
    private String type;
    private Hadith hadith;
    private FakeHadith fakeHadith;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
