package com.jamil.ahadith.features.upgrade.dto.request;

import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import lombok.Data;

@Data
public class UpgradeRequestDto {
    private UpgradeStatus status;

    private String filePath;
    private String notes;
}
