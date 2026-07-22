package com.jamil.ahadith.features.user.service;

import com.jamil.ahadith.core.storage.dto.ProfileImageResponse;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.exception.UserNotFoundException;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileTransactionService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void replaceProfileImage(UUID userId, ProfileImageResponse uploadedImage) {
        User user = userRepository.findByIdForUpdate(userId).orElseThrow(UserNotFoundException::new);
        String oldPublicId = user.getAvatarPublicId();
        String newPublicId = uploadedImage.getAvatarPublicId();
        user.setAvatarUrl(uploadedImage.getAvatarUrl());
        user.setAvatarPublicId(newPublicId);
        userRepository.save(user);
        eventPublisher.publishEvent(new ProfileImageChangedEvent(oldPublicId, newPublicId));
    }

    @Transactional
    public void clearProfileImage(UUID userId) {
        User user = userRepository.findByIdForUpdate(userId).orElseThrow(UserNotFoundException::new);
        String oldPublicId = user.getAvatarPublicId();
        user.setAvatarUrl(null);
        user.setAvatarPublicId(null);
        userRepository.save(user);
        eventPublisher.publishEvent(new ProfileImageChangedEvent(oldPublicId, null));
    }
}
