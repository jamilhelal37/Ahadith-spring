package com.jamil.ahadith.features.upgrade.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminUpgradeRequestResponseDto {
    private UUID id;
    private AdminUserReferenceDto user;
    private UpgradeStatus status;
    private AdminUserReferenceDto reviewedBy;
    private String notes;
    private String reviewNotes;
    private String rejectionReason;
    private boolean documentAvailable;
    private String documentOriginalName;
    private Long documentSizeBytes;
    private String documentResourceType;
    private String documentDeliveryType;
    private String documentFormat;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
