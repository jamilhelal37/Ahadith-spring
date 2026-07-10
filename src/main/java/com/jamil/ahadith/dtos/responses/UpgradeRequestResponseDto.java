package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.UpgradeStatus;
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
