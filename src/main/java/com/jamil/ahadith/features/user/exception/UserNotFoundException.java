package com.jamil.ahadith.features.user.exception;

import com.jamil.ahadith.features.user.entity.User;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User not found");
    }
}
