package com.jamil.ahadith.features.user.service;

import com.jamil.ahadith.core.storage.dto.ProfileImageResponse;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileTransactionServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserProfileTransactionService service;

    @Test
    void replaceProfileImage_ShouldUpdateUserAndPublishEvent() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setAvatarPublicId("old-id");
        
        ProfileImageResponse uploaded = new ProfileImageResponse("new-url", "new-id");
        
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        
        service.replaceProfileImage(userId, uploaded);
        
        assertEquals("new-id", user.getAvatarPublicId());
        assertEquals("new-url", user.getAvatarUrl());
        verify(userRepository).save(user);
        verify(eventPublisher).publishEvent(new ProfileImageChangedEvent("old-id", "new-id"));
    }

    @Test
    void clearProfileImage_ShouldClearAndPublishEvent() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setAvatarPublicId("old-id");
        user.setAvatarUrl("old-url");
        
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        
        service.clearProfileImage(userId);
        
        assertNull(user.getAvatarPublicId());
        assertNull(user.getAvatarUrl());
        verify(userRepository).save(user);
        verify(eventPublisher).publishEvent(new ProfileImageChangedEvent("old-id", null));
    }
}
