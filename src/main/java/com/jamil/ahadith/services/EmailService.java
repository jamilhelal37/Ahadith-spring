package com.jamil.ahadith.services;

import com.jamil.ahadith.entities.User;

public interface EmailService {
    void sendVerificationEmail(User user, String token);

    void sendPasswordResetEmail(User user, String token);
}
