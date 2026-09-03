package com.jamil.ahadith.features.notification.service;

import com.jamil.ahadith.features.notification.repository.UserFcmTokenRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFcmTokenServiceTest {
    @Mock
    private UserFcmTokenRepository userFcmTokenRepository;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserFcmTokenService service;

    @Test
    void registerCurrentUserTokenShouldUseAtomicUpsertWithNormalizedToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(currentUserService.requireCurrentUser()).thenReturn(user);

        service.registerCurrentUserToken("  token-value  ");

        ArgumentCaptor<LocalDateTime> lastSeenCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userFcmTokenRepository).upsertToken(org.mockito.ArgumentMatchers.eq(user.getId()),
                org.mockito.ArgumentMatchers.eq("token-value"), lastSeenCaptor.capture());
        assertThat(lastSeenCaptor.getValue()).isNotNull();
    }
}
