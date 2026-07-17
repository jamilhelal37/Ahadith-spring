package com.jamil.ahadith.core.mail;

import com.jamil.ahadith.features.user.entity.User;

public interface EmailService {
    void sendVerificationEmail(User user, String token);

    void sendPasswordResetEmail(User user, String token);
}
