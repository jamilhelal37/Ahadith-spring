package com.jamil.ahadith.features.search;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.features.search.repository.SearchHistoryRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class SearchHistoryRepositoryPostgresIT extends PostgresIntegrationTestBase {
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void deleteOldestForUserShouldUsePostgresNativeLimitAndPreserveOtherUsers() {
        User owner = user("history-owner-postgres@example.com");
        User other = user("history-other-postgres@example.com");
        UUID oldest = insertHistory(owner, "oldest", Instant.parse("2026-01-01T00:00:00Z"));
        UUID middle = insertHistory(owner, "middle", Instant.parse("2026-01-02T00:00:00Z"));
        UUID newest = insertHistory(owner, "newest", Instant.parse("2026-01-03T00:00:00Z"));
        UUID otherHistory = insertHistory(other, "other", Instant.parse("2026-01-01T00:00:00Z"));

        int deleted = searchHistoryRepository.deleteOldestForUser(owner.getId(), 2);

        assertThat(deleted).isEqualTo(2);
        assertThat(historyExists(oldest)).isFalse();
        assertThat(historyExists(middle)).isFalse();
        assertThat(historyExists(newest)).isTrue();
        assertThat(historyExists(otherHistory)).isTrue();
    }

    private User user(String email) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setStatus(UserStatus.active);
        user.setType(UserType.member);
        return userRepository.saveAndFlush(user);
    }

    private UUID insertHistory(User user, String text, Instant createdAt) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                """
                insert into public.search_history (id, user_id, search_text, search_source, created_at, updated_at)
                values (?, ?, ?, cast(? as public.search_source), ?, ?)
                """,
                id,
                user.getId(),
                text,
                "Hadith",
                Timestamp.from(createdAt),
                Timestamp.from(createdAt)
        );
        return id;
    }

    private boolean historyExists(UUID id) {
        Long count = jdbc.queryForObject(
                "select count(*) from public.search_history where id = ?",
                Long.class,
                id
        );
        return count != null && count > 0;
    }
}
