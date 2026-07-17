package com.jamil.ahadith.core.security;

import com.jamil.ahadith.features.user.entity.User;

public interface RefreshTokenRevoker {
    void revokeAllForUser(User user);
}
