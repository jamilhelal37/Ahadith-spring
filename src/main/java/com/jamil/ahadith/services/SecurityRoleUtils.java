package com.jamil.ahadith.services;

import com.jamil.ahadith.entities.UserType;

public final class SecurityRoleUtils {
    private SecurityRoleUtils() {
    }

    public static String authority(UserType type) {
        return "ROLE_" + type.name().toUpperCase();
    }

    public static String tokenRole(UserType type) {
        return type.name().toUpperCase();
    }
}
