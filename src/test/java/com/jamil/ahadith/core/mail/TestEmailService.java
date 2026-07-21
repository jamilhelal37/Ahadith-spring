package com.jamil.ahadith.core.mail;

import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.core.mail.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Profile("test")
public class TestEmailService implements EmailService {
    private final Map<String, String> verificationTokens = new ConcurrentHashMap<>();
    private final Map<String, String> passwordResetTokens = new ConcurrentHashMap<>();
    private final AtomicBoolean failNextPasswordReset = new AtomicBoolean();

    @Override
    public void sendVerificationEmail(User user, String token) {
        verificationTokens.put(user.getEmail(), token);
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        if (failNextPasswordReset.compareAndSet(true, false)) {
            throw new IllegalStateException("Test password reset email failure");
        }
        passwordResetTokens.put(user.getEmail(), token);
    }

    public String verificationTokenFor(String email) {
        return verificationTokens.get(email);
    }

    public String passwordResetTokenFor(String email) {
        return passwordResetTokens.get(email);
    }

    public void failNextPasswordResetEmail() {
        failNextPasswordReset.set(true);
    }

    public void clear() {
        verificationTokens.clear();
        passwordResetTokens.clear();
        failNextPasswordReset.set(false);
    }
}
