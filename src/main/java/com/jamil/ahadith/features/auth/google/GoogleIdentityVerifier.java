package com.jamil.ahadith.features.auth.google;

public interface GoogleIdentityVerifier {
    GoogleIdentity verify(String idToken);
}
