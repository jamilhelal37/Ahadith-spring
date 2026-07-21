package com.jamil.ahadith.features.notification.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.FakeHadithReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponseDto {
    private UUID id;
    private String title;
    private String body;
    private String type;
    private HadithReferenceResponseDto hadith;
    private FakeHadithReferenceResponseDto fakeHadith;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
