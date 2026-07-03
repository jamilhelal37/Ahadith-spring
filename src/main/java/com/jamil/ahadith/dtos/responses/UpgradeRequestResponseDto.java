package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.UpgradeStatus;
import com.jamil.ahadith.entities.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpgradeRequestResponseDto {
    private UUID id;
    private User user;
    private UpgradeStatus status;
    private String filePath;
    private User reviewedBy;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
