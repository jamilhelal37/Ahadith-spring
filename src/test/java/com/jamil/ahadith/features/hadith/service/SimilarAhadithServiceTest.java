package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.hadith.dto.request.SimilarAhadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.SimilarAhadithResponseDto;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.entity.SimilarAhadith;
import com.jamil.ahadith.features.hadith.mapper.SimilarAhadithMapper;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.hadith.repository.SimilarAhadithRepository;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimilarAhadithServiceTest {

    @Mock
    private SimilarAhadithRepository similarAhadithRepository;

    @Mock
    private SimilarAhadithMapper similarAhadithMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private HadithRepository hadithRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @Mock
    private HadithSearchService hadithSearchService;

    private SimilarAhadithService service;

    @BeforeEach
    void setUp() {
        service = new SimilarAhadithService(
                similarAhadithRepository,
                similarAhadithMapper,
                entityManager,
                new AdminPageService(),
                hadithRepository,
                currentUserService,
                auditEventPublisher,
                hadithSearchService
        );

        lenient()
                .when(similarAhadithMapper.toResponseDto(any(SimilarAhadith.class)))
                .thenAnswer(invocation -> new SimilarAhadithResponseDto());
    }

    @Test
    void getSimilarAhadithsShouldBulkLoadMainAndSimilarHadithsOnce() {
        UUID mainId = UUID.randomUUID();
        UUID firstSimilarId = UUID.randomUUID();
        UUID secondSimilarId = UUID.randomUUID();
        var first = similarAhadith(mainId, firstSimilarId);
        var second = similarAhadith(mainId, secondSimilarId);
        var pageable = PageRequest.of(0, 10);

        when(similarAhadithRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));
        when(hadithSearchService.getHadithCardsByIdsInOrder(anyList()))
                .thenReturn(List.of(
                        searchItem(mainId, "Main full"),
                        searchItem(firstSimilarId, "First similar full"),
                        searchItem(secondSimilarId, "Second similar full")
                ));

        var response = service.getSimilarAhadiths(pageable);

        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getMainHadith().getText())
                .isEqualTo("Main full");
        assertThat(response.getItems().get(0).getSimHadith().getText())
                .isEqualTo("First similar full");
        assertThat(response.getItems().get(1).getMainHadith().getText())
                .isEqualTo("Main full");
        assertThat(response.getItems().get(1).getSimHadith().getText())
                .isEqualTo("Second similar full");

        ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(hadithSearchService).getHadithCardsByIdsInOrder(idsCaptor.capture());
        assertThat(idsCaptor.getValue())
                .containsExactly(mainId, firstSimilarId, secondSimilarId);
    }

    @Test
    void getSimilarAhadithByIdShouldKeepNullReferencesAndSkipSearch() {
        UUID id = UUID.randomUUID();
        var similarAhadith = new SimilarAhadith();
        similarAhadith.setId(id);

        when(similarAhadithRepository.findById(id))
                .thenReturn(Optional.of(similarAhadith));

        var response = service.getSimilarAhadithById(id);

        assertThat(response.getMainHadith()).isNull();
        assertThat(response.getSimHadith()).isNull();
        verify(hadithSearchService, never()).getHadithCardsByIdsInOrder(anyList());
    }

    @Test
    void createSimilarAhadithShouldPopulateBothReferencesFromSearchResult() {
        UUID mainId = UUID.randomUUID();
        UUID similarId = UUID.randomUUID();
        var request = request(mainId, similarId);
        var entity = new SimilarAhadith();

        when(similarAhadithMapper.toEntity(request)).thenReturn(entity);
        when(hadithRepository.findById(mainId)).thenReturn(Optional.of(hadith(mainId)));
        when(hadithRepository.findById(similarId)).thenReturn(Optional.of(hadith(similarId)));
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());
        when(similarAhadithRepository.saveAndFlush(entity)).thenReturn(entity);
        when(hadithSearchService.getHadithCardsByIdsInOrder(List.of(mainId, similarId)))
                .thenReturn(List.of(
                        searchItem(mainId, "Main full"),
                        searchItem(similarId, "Similar full")
                ));

        var response = service.createSimilarAhadith(request);

        assertThat(response.getMainHadith().getId()).isEqualTo(mainId);
        assertThat(response.getMainHadith().getText()).isEqualTo("Main full");
        assertThat(response.getSimHadith().getId()).isEqualTo(similarId);
        assertThat(response.getSimHadith().getText()).isEqualTo("Similar full");
    }

    private SimilarAhadith similarAhadith(UUID mainId, UUID similarId) {
        var similarAhadith = new SimilarAhadith();
        similarAhadith.setId(UUID.randomUUID());
        similarAhadith.setMainHadith(hadith(mainId));
        similarAhadith.setSimHadith(hadith(similarId));
        return similarAhadith;
    }

    private Hadith hadith(UUID id) {
        var hadith = new Hadith();
        hadith.setId(id);
        return hadith;
    }

    private SimilarAhadithRequestDto request(UUID mainId, UUID similarId) {
        var request = new SimilarAhadithRequestDto();
        request.setMainHadith(reference(mainId));
        request.setSimHadith(reference(similarId));
        return request;
    }

    private HadithReferenceRequestDto reference(UUID id) {
        var reference = new HadithReferenceRequestDto();
        reference.setId(id);
        return reference;
    }

    private HadithSearchItemDto searchItem(UUID id, String text) {
        return new HadithSearchItemDto(
                id,
                text,
                "Normal " + text,
                5,
                "marfu",
                "Sanad " + text,
                null,
                null,
                null,
                null,
                List.of(),
                true,
                false
        );
    }
}
