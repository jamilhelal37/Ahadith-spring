package com.jamil.ahadith.features.upgrade.service;

public record UpgradeRequestCreatedEvent(com.jamil.ahadith.features.upgrade.entity.UpgradeRequest request, String publicId) {
}