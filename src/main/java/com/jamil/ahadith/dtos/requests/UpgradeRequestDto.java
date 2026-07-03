package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.UpgradeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpgradeRequestDto {
    @NotNull(message = "Status is required")
    private UpgradeStatus status;

    private String filePath;
    private String notes;
}
