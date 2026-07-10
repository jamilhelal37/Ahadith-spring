package com.jamil.ahadith;

import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.services.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("test")
public class TestEmailService implements EmailService {
    private final Map<String, String> verificationTokens = new ConcurrentHashMap<>();
    private final Map<String, String> passwordResetTokens = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationEmail(User user, String token) {
        verificationTokens.put(user.getEmail(), token);
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        passwordResetTokens.put(user.getEmail(), token);
    }

    public String verificationTokenFor(String email) {
        return verificationTokens.get(email);
    }

    public String passwordResetTokenFor(String email) {
        return passwordResetTokens.get(email);
    }

    public void clear() {
        verificationTokens.clear();
        passwordResetTokens.clear();
    }
}
