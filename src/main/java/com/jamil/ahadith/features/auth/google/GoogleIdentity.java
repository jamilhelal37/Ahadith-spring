package com.jamil.ahadith.features.auth.google;

public record GoogleIdentity(
        String subject,
        String email,
        boolean emailVerified,
        String name,
        String pictureUrl
) {
}
