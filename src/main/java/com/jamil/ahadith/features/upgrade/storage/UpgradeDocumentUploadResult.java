package com.jamil.ahadith.features.upgrade.storage;

public record UpgradeDocumentUploadResult(
        String assetId,
        String publicId,
        String resourceType,
        String deliveryType,
        String format,
        String originalFileName,
        long sizeBytes
) {
}
