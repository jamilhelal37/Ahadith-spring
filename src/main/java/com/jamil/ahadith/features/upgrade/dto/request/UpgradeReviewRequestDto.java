package com.jamil.ahadith.features.upgrade.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpgradeReviewRequestDto {
    @NotNull
    private UpgradeDecision decision;
    @Size(max = ValidationLimits.NOTES_MAX)
    private String reviewNotes;
    @Size(max = ValidationLimits.NOTES_MAX)
    private String rejectionReason;
}
