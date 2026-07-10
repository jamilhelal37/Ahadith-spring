package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.FakeHadith;
import com.jamil.ahadith.entities.Hadith;
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
