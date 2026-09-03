package com.jamil.ahadith.features.account.event;

import com.jamil.ahadith.features.user.entity.User;

public record AccountEmailEvent(User user, String rawToken, EmailEventType type) {
    
    public enum EmailEventType {
        VERIFICATION,
        PASSWORD_RESET
    }

    @Override
    public String toString() {
        return "AccountEmailEvent{" +
                "user=" + (user != null ? user.getEmail() : "null") +
                ", type=" + type +
                ", rawToken='[PROTECTED]'" +
                '}';
    }
}
