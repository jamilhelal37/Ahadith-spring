package com.jamil.ahadith.core.validation;

import java.util.Locale;

public class EmailNormalizer {
    
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
