package com.jamil.ahadith.core.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jamil.ahadith.core.storage.dto.ProfileImageResponse;
import com.jamil.ahadith.core.storage.exception.ProfileImageStorageException;
import com.jamil.ahadith.core.storage.exception.ProfileImageValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CloudinaryStorageService {
    private static final long MAX_PROFILE_IMAGE_SIZE = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_PROFILE_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Cloudinary cloudinary;

    public ProfileImageResponse uploadProfileImage(MultipartFile file, UUID userId) {
        validateProfileImage(file);

        String publicId = "users/%s/profile/avatar".formatted(userId);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "image"
            ));
            return new ProfileImageResponse(
                    String.valueOf(result.get("secure_url")),
                    String.valueOf(result.get("public_id"))
            );
        } catch (IOException ex) {
            throw new ProfileImageStorageException("Failed to upload profile image", ex);
        }
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException ex) {
            throw new ProfileImageStorageException("Failed to delete profile image", ex);
        }
    }

    private void validateProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProfileImageValidationException("Profile image file is required");
        }
        if (!ALLOWED_PROFILE_IMAGE_TYPES.contains(file.getContentType())) {
            throw new ProfileImageValidationException("Profile image must be image/jpeg, image/png, or image/webp");
        }
        if (file.getSize() > MAX_PROFILE_IMAGE_SIZE) {
            throw new ProfileImageValidationException("Profile image size must not exceed 2MB");
        }
    }
}
