package com.jamil.ahadith.features.user.service;

import com.jamil.ahadith.core.storage.CloudinaryStorageService;

import com.jamil.ahadith.core.storage.dto.ProfileImageResponse;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.exception.UserNotFoundException;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    @Transactional
    public ProfileImageResponse uploadProfileImage(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        ProfileImageResponse uploadedImage = cloudinaryStorageService.uploadProfileImage(file, userId);

        user.setAvatarUrl(uploadedImage.getAvatarUrl());
        user.setAvatarPublicId(uploadedImage.getAvatarPublicId());
        userRepository.save(user);

        return uploadedImage;
    }

    @Transactional
    public void deleteProfileImage(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (user.getAvatarPublicId() != null && !user.getAvatarPublicId().isBlank()) {
            cloudinaryStorageService.deleteImage(user.getAvatarPublicId());
        }

        user.setAvatarUrl(null);
        user.setAvatarPublicId(null);
        userRepository.save(user);
    }
}
