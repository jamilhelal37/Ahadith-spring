package com.jamil.ahadith.services;

import com.jamil.ahadith.entities.UserType;

public final class SecurityRoleUtils {
    private SecurityRoleUtils() {
    }

    public static String authority(UserType type) {
        if (type == null) {
            return "ROLE_MEMBER";
        }
        if (type == UserType.scholar) {
            return "ROLE_ADMIN";
        }
        return "ROLE_" + type.name().toUpperCase();
    }

    public static String tokenRole(UserType type) {
        if (type == null) {
            return UserType.member.name().toUpperCase();
        }
        if (type == UserType.scholar) {
            return UserType.admin.name().toUpperCase();
        }
        return type.name().toUpperCase();
    }

    public static boolean isAdminAuthority(String authority) {
        return authority(UserType.admin).equals(authority);
    }

    public static boolean isSupervisorAuthority(String authority) {
        return authority(UserType.supervisor).equals(authority);
    }

    public static boolean isMemberAuthority(String authority) {
        return authority(UserType.member).equals(authority);
    }
}
