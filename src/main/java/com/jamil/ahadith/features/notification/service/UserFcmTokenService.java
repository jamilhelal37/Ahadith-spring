package com.jamil.ahadith.features.notification.service;

import com.jamil.ahadith.features.notification.repository.UserFcmTokenRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserFcmTokenService {
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public void registerCurrentUserToken(String rawToken) {
        User user = currentUserService.requireCurrentUser();
        String token = normalizeToken(rawToken);
        LocalDateTime now = LocalDateTime.now();

        userFcmTokenRepository.upsertToken(user.getId(), token, now);
    }

    @Transactional
    public void deleteCurrentUserToken(String rawToken) {
        User user = currentUserService.requireCurrentUser();
        userFcmTokenRepository.deleteByUserAndFcmToken(user, normalizeToken(rawToken));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long deleteInvalidTokens(Collection<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return 0;
        }
        return userFcmTokenRepository.deleteByFcmTokenIn(tokens);
    }

    private String normalizeToken(String token) {
        return token == null ? null : token.trim();
    }
}
