package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.hadith.dto.update.HadithUpdateDto;
import com.jamil.ahadith.features.hadith.dto.response.HadithResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import com.jamil.ahadith.features.hadith.mapper.HadithMapper;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.hadith.repository.ExplainingRepository;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.search.semantic.event.HadithTextChangedEvent;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HadithServiceUpdateTest {

    @Mock
    private HadithRepository hadithRepository;
    @Mock
    private HadithMapper hadithMapper;
    @Mock
    private ExplainingRepository explainingRepository;
    @Mock
    private RulingRepository rulingRepository;
    @Mock
    private RawiRepository rawiRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private AuditEventPublisher auditEventPublisher;
    @Mock
    private HadithSearchService hadithSearchService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private HadithService hadithService;

    private Hadith hadith;
    private UUID hadithId;

    @BeforeEach
    void setUp() {
        hadithId = UUID.randomUUID();
        hadith = new Hadith();
        hadith.setId(hadithId);
        hadith.setText("Original Text which is long enough");
        hadith.setType(HadithType.qudsi);
        hadith.setHadithNumber(1);

        lenient()
                .when(hadithMapper.toResponseDto(any(Hadith.class)))
                .thenReturn(new HadithResponseDto());
    }

    @Test
    void updateHadith_ShouldThrowException_WhenNoFieldsProvided() {
        HadithUpdateDto updateDto = new HadithUpdateDto();
        assertThrows(InvalidRequestException.class, () -> hadithService.updateHadith(hadithId, updateDto));
    }

    @Test
    void updateHadith_ShouldThrowException_WhenAllFieldsAreNull() {
        HadithUpdateDto updateDto = new HadithUpdateDto();
        updateDto.setText(null);
        updateDto.setType(null);
        assertThrows(InvalidRequestException.class, () -> hadithService.updateHadith(hadithId, updateDto));
    }

    @Test
    void updateHadith_ShouldUpdatePartially_WhenSomeFieldsProvided() {
        HadithUpdateDto updateDto = new HadithUpdateDto();
        updateDto.setText("New Updated Text which is also long enough");

        when(hadithRepository.findById(hadithId)).thenReturn(Optional.of(hadith));
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());
        when(hadithRepository.saveAndFlush(any())).thenReturn(hadith);

        hadithService.updateHadith(hadithId, updateDto);

        verify(hadithMapper).updateEntity(updateDto, hadith);
        verify(hadithRepository).saveAndFlush(hadith);
        verify(hadithSearchService, never()).getHadithCardsByIdsInOrder(anyList());
    }

    @Test
    void changingTextPublishesEmbeddingEvent() {
        HadithUpdateDto updateDto = new HadithUpdateDto();
        updateDto.setText("Changed Text which is long enough");
        when(hadithRepository.findById(hadithId)).thenReturn(Optional.of(hadith));
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());
        when(hadithRepository.saveAndFlush(hadith)).thenReturn(hadith);
        doAnswer(invocation -> {
            hadith.setText(updateDto.getText());
            return null;
        }).when(hadithMapper).updateEntity(updateDto, hadith);

        hadithService.updateHadith(hadithId, updateDto);

        verify(eventPublisher).publishEvent(new HadithTextChangedEvent(hadithId, updateDto.getText()));
    }

    @Test
    void metadataOnlyChangeDoesNotPublishEmbeddingEvent() {
        HadithUpdateDto updateDto = new HadithUpdateDto();
        updateDto.setType(HadithType.marfu);
        when(hadithRepository.findById(hadithId)).thenReturn(Optional.of(hadith));
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());
        when(hadithRepository.saveAndFlush(hadith)).thenReturn(hadith);

        hadithService.updateHadith(hadithId, updateDto);

        verify(eventPublisher, never()).publishEvent(any(HadithTextChangedEvent.class));
    }

    @Test
    void updateHadith_ShouldThrowException_WhenReferencingItselfAsSubValid() {
        HadithUpdateDto updateDto = new HadithUpdateDto();
        HadithReferenceRequestDto ref = new HadithReferenceRequestDto();
        ref.setId(hadithId);
        updateDto.setSubValid(ref);

        when(hadithRepository.findById(hadithId)).thenReturn(Optional.of(hadith));

        assertThrows(InvalidRequestException.class, () -> hadithService.updateHadith(hadithId, updateDto));
    }

    @Test
    void updateHadith_ShouldProceed_WhenReferencingDifferentHadithAsSubValid() {
        UUID otherId = UUID.randomUUID();
        HadithUpdateDto updateDto = new HadithUpdateDto();
        HadithReferenceRequestDto ref = new HadithReferenceRequestDto();
        ref.setId(otherId);
        updateDto.setSubValid(ref);

        Hadith otherHadith = new Hadith();
        otherHadith.setId(otherId);

        when(hadithRepository.findById(hadithId)).thenReturn(Optional.of(hadith));
        when(hadithRepository.findById(otherId)).thenReturn(Optional.of(otherHadith));
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());
        when(hadithRepository.saveAndFlush(any())).thenReturn(hadith);
        when(hadithSearchService.getHadithCardsByIdsInOrder(List.of(otherId)))
                .thenReturn(List.of(searchItem(otherId)));

        HadithResponseDto response = hadithService.updateHadith(hadithId, updateDto);

        verify(hadithRepository).findById(otherId);
        assertEquals(otherHadith, hadith.getSubValid());
        assertEquals(otherId, response.getSubValid().getId());
        assertEquals("Full text", response.getSubValid().getText());
    }

    @Test
    void getHadithById_ShouldKeepSubValidNull_WhenNoSubValidExists() {
        when(hadithRepository.findById(hadithId)).thenReturn(Optional.of(hadith));

        HadithResponseDto response = hadithService.getHadithById(hadithId);

        assertNull(response.getSubValid());
        verify(hadithSearchService, never()).getHadithCardsByIdsInOrder(anyList());
    }

    @Test
    void getHadithById_ShouldSetFullSubValidReference_WhenSubValidExists() {
        UUID subValidId = UUID.randomUUID();
        Hadith subValid = new Hadith();
        subValid.setId(subValidId);
        hadith.setSubValid(subValid);

        when(hadithRepository.findById(hadithId)).thenReturn(Optional.of(hadith));
        when(hadithSearchService.getHadithCardsByIdsInOrder(List.of(subValidId)))
                .thenReturn(List.of(searchItem(subValidId)));

        HadithResponseDto response = hadithService.getHadithById(hadithId);

        HadithReferenceResponseDto reference = response.getSubValid();
        assertNotNull(reference);
        assertEquals(subValidId, reference.getId());
        assertEquals("Full text", reference.getText());
        assertEquals("Normal text", reference.getNormalText());
        assertEquals(22, reference.getHadithNumber());
        assertEquals("marfu", reference.getType());
        assertEquals("Full sanad", reference.getSanad());
        assertTrue(reference.isHasExplanation());
        assertFalse(reference.isHasSubValid());
    }

    private HadithSearchItemDto searchItem(UUID id) {
        return new HadithSearchItemDto(
                id,
                "Full text",
                "Normal text",
                22,
                "marfu",
                "Full sanad",
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
