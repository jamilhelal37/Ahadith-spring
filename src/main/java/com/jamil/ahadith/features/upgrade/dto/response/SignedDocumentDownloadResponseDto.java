package com.jamil.ahadith.features.upgrade.dto.response;

import java.time.Instant;

public record SignedDocumentDownloadResponseDto(String downloadUrl, Instant expiresAt) {
}
