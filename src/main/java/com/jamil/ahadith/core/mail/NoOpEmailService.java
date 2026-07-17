package com.jamil.ahadith.core.mail;

import com.jamil.ahadith.features.user.entity.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEmailService implements EmailService {
    @Override
    public void sendVerificationEmail(User user, String token) {
        // Email is intentionally disabled for this profile.
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        // Email is intentionally disabled for this profile.
    }
}
