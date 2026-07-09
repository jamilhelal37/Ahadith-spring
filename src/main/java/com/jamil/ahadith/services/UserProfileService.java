package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.responses.ProfileImageResponse;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.exceptions.UserNotFoundException;
import com.jamil.ahadith.repositories.UserRepository;
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
