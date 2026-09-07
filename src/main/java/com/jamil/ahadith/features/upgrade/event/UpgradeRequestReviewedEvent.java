package com.jamil.ahadith.features.upgrade.event;

import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;

import java.util.UUID;

public record UpgradeRequestReviewedEvent(
        UUID userId,
        UpgradeStatus status
) {
}