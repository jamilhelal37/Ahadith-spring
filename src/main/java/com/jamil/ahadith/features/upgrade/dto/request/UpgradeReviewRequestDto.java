package com.jamil.ahadith.features.upgrade.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpgradeReviewRequestDto {
    @NotNull
    private UpgradeDecision decision;
    private String reviewNotes;
    private String rejectionReason;
}
