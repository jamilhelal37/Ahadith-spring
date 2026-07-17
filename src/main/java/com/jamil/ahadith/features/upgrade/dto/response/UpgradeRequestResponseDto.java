package com.jamil.ahadith.features.upgrade.dto.response;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;

import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpgradeRequestResponseDto {
    private UUID id;
    private AdminUserReferenceDto user;
    private UpgradeStatus status;
    private String filePath;
    private AdminUserReferenceDto reviewedBy;
    private String notes;
    private String reviewNotes;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
