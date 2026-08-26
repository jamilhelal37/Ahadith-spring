package com.jamil.ahadith.features.audit;

import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditDataTest {

    @Test
    void snapshotShouldNotExposeSensitiveUserFields() {
        User user = new User();
        user.setName("User");
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setGoogleSubject("google-subject");
        user.setTokenVersion(3);

        var snapshot = AuditData.snapshot(user);

        assertThat(snapshot).doesNotContainKeys("password", "googleSubject");
        assertThat(snapshot.keySet()).noneMatch(key -> key.toLowerCase().contains("token"));
    }
}
