package com.jamil.ahadith.features.upgrade.dto.response;

import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MemberUpgradeRequestResponseDto {
    private UUID id;
    private UpgradeStatus status;
    private String notes;
    private String reviewNotes;
    private String rejectionReason;
    private boolean documentAvailable;
    private String documentOriginalName;
    private Long documentSizeBytes;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
