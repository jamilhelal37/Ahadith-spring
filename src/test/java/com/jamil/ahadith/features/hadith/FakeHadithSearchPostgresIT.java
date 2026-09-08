package com.jamil.ahadith.features.hadith;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FakeHadithSearchPostgresIT extends PostgresIntegrationTestBase {

    @Autowired
    private FakeHadithRepository fakeHadithRepository;

    @Test
    void searchShouldWorkWithArabicNormalizationAndCreatedAtSort() {
        UUID olderId = UUID.randomUUID();
        UUID newerId = UUID.randomUUID();

        jdbc.update("""
                insert into public.fake_ahadith
                    (id, text, created_at, updated_at)
                values (?, ?, ?, ?)
                """,
                olderId,
                "اختلاف أمتي رحمة",
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-01T10:00:00Z")
        );

        jdbc.update("""
                insert into public.fake_ahadith
                    (id, text, created_at, updated_at)
                values (?, ?, ?, ?)
                """,
                newerId,
                "اختلاف امتي نعمة",
                OffsetDateTime.parse("2026-01-02T10:00:00Z"),
                OffsetDateTime.parse("2026-01-02T10:00:00Z")
        );

        var result = fakeHadithRepository.searchByText(
                "اختلاف أمتي",
                PageRequest.of(
                        0,
                        20,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                                .and(Sort.by(Sort.Direction.ASC, "id"))
                )
        );

        assertThat(result.getContent())
                .extracting(FakeHadith::getId)
                .containsExactly(newerId, olderId);
    }
}