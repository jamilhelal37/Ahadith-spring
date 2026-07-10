package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.UpgradeStatus;
import lombok.Data;

@Data
public class UpgradeRequestDto {
    private UpgradeStatus status;

    private String filePath;
    private String notes;
}
