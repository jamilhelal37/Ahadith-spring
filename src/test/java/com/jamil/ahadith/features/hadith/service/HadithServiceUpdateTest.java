package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.hadith.dto.update.HadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import com.jamil.ahadith.features.hadith.mapper.HadithMapper;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.hadith.repository.ExplainingRepository;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
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

        hadithService.updateHadith(hadithId, updateDto);

        verify(hadithRepository).findById(otherId);
        assertEquals(otherHadith, hadith.getSubValid());
    }
}
