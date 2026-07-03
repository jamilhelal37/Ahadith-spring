package com.jamil.ahadith.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    male, female;

    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (Gender gender : values()) {
            if (gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }

        throw new IllegalArgumentException("Invalid gender: " + value);
    }

    @JsonValue
    public String getValue() {
        return name();
    }
}
