package com.jamil.ahadith.features.account.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {
    public void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new InvalidRequestException("Password must be at least 8 characters");
        }
    }
}
