package com.jamil.ahadith.features.user.service;

import com.jamil.ahadith.core.storage.CloudinaryStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileImageCleanupListenerTest {

    @Mock
    private CloudinaryStorageService cloudinaryStorageService;

    @InjectMocks
    private ProfileImageCleanupListener listener;

    @Test
    void deleteOldImage_ShouldDelete_WhenOldPublicIdIsDifferentFromNew() {
        ProfileImageChangedEvent event = new ProfileImageChangedEvent("old-id", "new-id");
        listener.deleteOldImage(event);
        verify(cloudinaryStorageService).deleteImage("old-id");
    }

    @Test
    void deleteOldImage_ShouldNotDelete_WhenOldPublicIdIsNull() {
        ProfileImageChangedEvent event = new ProfileImageChangedEvent(null, "new-id");
        listener.deleteOldImage(event);
        verify(cloudinaryStorageService, never()).deleteImage(anyString());
    }

    @Test
    void deleteOldImage_ShouldNotDelete_WhenOldPublicIdEqualsNew() {
        ProfileImageChangedEvent event = new ProfileImageChangedEvent("same-id", "same-id");
        listener.deleteOldImage(event);
        verify(cloudinaryStorageService, never()).deleteImage(anyString());
    }

    @Test
    void deleteOldImage_ShouldNotThrow_WhenCloudinaryFails() {
        ProfileImageChangedEvent event = new ProfileImageChangedEvent("old-id", "new-id");
        doThrow(new RuntimeException("Cloudinary error")).when(cloudinaryStorageService).deleteImage("old-id");
        
        listener.deleteOldImage(event);
        
        verify(cloudinaryStorageService).deleteImage("old-id");
        // No exception should be thrown to caller
    }
}
