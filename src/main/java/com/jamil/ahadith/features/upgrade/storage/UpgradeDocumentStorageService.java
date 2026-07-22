package com.jamil.ahadith.features.upgrade.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

public interface UpgradeDocumentStorageService {
    UpgradeDocumentUploadResult upload(MultipartFile file, UUID userId);

    String createDownloadUrl(String publicId, Instant expiresAt);

    void delete(String publicId);
}
