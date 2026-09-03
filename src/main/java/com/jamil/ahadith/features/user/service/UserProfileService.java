package com.jamil.ahadith.features.user.service;

import com.jamil.ahadith.core.storage.CloudinaryStorageService;

import com.jamil.ahadith.core.storage.dto.ProfileImageResponse;
import com.jamil.ahadith.core.web.dto.MessageResponseDto;
import com.jamil.ahadith.features.auth.dto.response.AuthUserDto;
import com.jamil.ahadith.features.user.dto.request.ChangePasswordRequestDto;
import com.jamil.ahadith.features.user.dto.request.UserProfileUpdateRequestDto;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserProfileService {
    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    private final CloudinaryStorageService cloudinaryStorageService;
    private final UserProfileTransactionService userProfileTransactionService;

    public ProfileImageResponse uploadProfileImage(UUID userId, MultipartFile file) {
        ProfileImageResponse uploadedImage = cloudinaryStorageService.uploadProfileImage(file, userId);
        try {
            userProfileTransactionService.replaceProfileImage(userId, uploadedImage);
        } catch (RuntimeException ex) {
            try {
                cloudinaryStorageService.deleteImage(uploadedImage.getAvatarPublicId());
            } catch (RuntimeException cleanupFailure) {
                log.warn("Failed to delete uploaded profile image after database update failure for userId: {} and publicId: {}", 
                    userId, uploadedImage.getAvatarPublicId(), cleanupFailure);
            }
            throw ex;
        }
        return uploadedImage;
    }

    public void deleteProfileImage(UUID userId) {
        userProfileTransactionService.clearProfileImage(userId);
    }

    public AuthUserDto updateProfile(UUID userId, UserProfileUpdateRequestDto request) {
        return userProfileTransactionService.updateProfile(userId, request);
    }

    public MessageResponseDto changePassword(UUID userId, ChangePasswordRequestDto request) {
        return userProfileTransactionService.changePassword(userId, request);
    }
}
