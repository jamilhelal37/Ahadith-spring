package com.jamil.ahadith.services;

import com.jamil.ahadith.exceptions.InvalidRequestException;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {
    public void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new InvalidRequestException("Password must be at least 8 characters");
        }
    }
}
