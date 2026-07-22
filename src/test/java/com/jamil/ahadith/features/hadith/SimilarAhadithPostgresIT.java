package com.jamil.ahadith.features.hadith;

import com.jamil.ahadith.core.PostgresIntegrationTestBase;
import com.jamil.ahadith.core.exception.ConflictException;
import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.hadith.dto.request.SimilarAhadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.update.SimilarAhadithUpdateDto;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.repository.SimilarAhadithRepository;
import com.jamil.ahadith.features.hadith.service.SimilarAhadithService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimilarAhadithPostgresIT extends PostgresIntegrationTestBase {
    @Autowired
    private SimilarAhadithService service;

    @Autowired
    private SimilarAhadithRepository repository;

    @Test
    void createAndUpdateShouldPopulateAuditTimestampsFromPostgres() {
        UUID first = hadith(1, "First hadith");
        UUID second = hadith(2, "Second hadith");
        UUID third = hadith(3, "Third hadith");

        var created = service.createSimilarAhadith(request(first, second));
        var createdEntity = repository.findById(created.getId()).orElseThrow();

        assertThat(createdEntity.getCreatedAt()).isNotNull();
        assertThat(createdEntity.getUpdatedAt()).isNotNull();

        LocalDateTime beforeUpdate = createdEntity.getUpdatedAt();
        var update = new SimilarAhadithUpdateDto();
        update.setSimHadith(reference(third));

        service.updateSimilarAhadith(created.getId(), update);

        var updatedEntity = repository.findById(created.getId()).orElseThrow();
        assertThat(updatedEntity.getMainHadith().getId()).isEqualTo(first);
        assertThat(updatedEntity.getSimHadith().getId()).isEqualTo(third);
        assertThat(updatedEntity.getCreatedAt()).isEqualTo(createdEntity.getCreatedAt());
        assertThat(updatedEntity.getUpdatedAt()).isNotNull();
        assertThat(updatedEntity.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    void createShouldRejectSelfRelationship() {
        UUID hadith = hadith(1, "Self hadith");

        assertThatThrownBy(() -> service.createSimilarAhadith(request(hadith, hadith)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void createShouldRejectDuplicateRelationship() {
        UUID first = hadith(1, "First hadith");
        UUID second = hadith(2, "Second hadith");
        service.createSimilarAhadith(request(first, second));

        assertThatThrownBy(() -> service.createSimilarAhadith(request(first, second)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createShouldReturnNotFoundWhenEitherHadithDoesNotExist() {
        UUID first = hadith(1, "First hadith");

        assertThatThrownBy(() -> service.createSimilarAhadith(request(first, uuid(404))))
                .isInstanceOf(HadithNotFoundException.class);

        assertThatThrownBy(() -> service.createSimilarAhadith(request(uuid(405), first)))
                .isInstanceOf(HadithNotFoundException.class);
    }

    private SimilarAhadithRequestDto request(UUID mainHadithId, UUID simHadithId) {
        var request = new SimilarAhadithRequestDto();
        request.setMainHadith(reference(mainHadithId));
        request.setSimHadith(reference(simHadithId));
        return request;
    }

    private HadithReferenceRequestDto reference(UUID id) {
        var reference = new HadithReferenceRequestDto();
        reference.setId(id);
        return reference;
    }

    private UUID hadith(int id, String text) {
        UUID uuid = uuid(id);
        jdbc.update("""
                insert into public.ahadith (id, text, hadith_number, type)
                values (?, ?, ?, cast(? as public.hadith_type))
                """, uuid, text, id, "marfu");
        return uuid;
    }

    private UUID uuid(int value) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(value));
    }
}
