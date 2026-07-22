package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.core.web.dto.TriState;
import com.jamil.ahadith.features.hadith.dto.update.HadithPatchDto;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HadithServicePatchTest {

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

    @BeforeEach
    void setUp() {
        hadith = new Hadith();
        hadith.setId(UUID.randomUUID());
        hadith.setText("Original Text");
        hadith.setType(HadithType.qudsi);
        hadith.setHadithNumber(1);
    }

    @Test
    void applyPatch_ShouldThrowException_WhenNoFieldsDefined() {
        HadithPatchDto patchDto = new HadithPatchDto();
        org.mockito.Mockito.when(hadithRepository.findById(hadith.getId())).thenReturn(java.util.Optional.of(hadith));
        assertThrows(InvalidRequestException.class, () -> hadithService.patchHadith(hadith.getId(), patchDto));
    }

    @Test
    void applyPatch_ShouldUpdateType_WhenDefined() {
        HadithPatchDto patchDto = new HadithPatchDto();
        patchDto.setType(TriState.defined(HadithType.marfu));
        
        // We need to mock findById since patchHadith calls it
        org.mockito.Mockito.when(hadithRepository.findById(hadith.getId())).thenReturn(java.util.Optional.of(hadith));
        org.mockito.Mockito.when(currentUserService.getCurrentUser()).thenReturn(java.util.Optional.empty());
        org.mockito.Mockito.when(hadithRepository.saveAndFlush(org.mockito.ArgumentMatchers.any())).thenReturn(hadith);

        hadithService.patchHadith(hadith.getId(), patchDto);

        assertEquals(HadithType.marfu, hadith.getType());
    }

    @Test
    void applyPatch_ShouldThrowException_WhenTypeIsNull() {
        HadithPatchDto patchDto = new HadithPatchDto();
        patchDto.setType(TriState.defined(null));
        org.mockito.Mockito.when(hadithRepository.findById(hadith.getId())).thenReturn(java.util.Optional.of(hadith));

        assertThrows(InvalidRequestException.class, () -> hadithService.patchHadith(hadith.getId(), patchDto));
    }

    @Test
    void applyPatch_ShouldUpdateText_WhenValid() {
        HadithPatchDto patchDto = new HadithPatchDto();
        patchDto.setText(TriState.defined("New Valid Text"));
        org.mockito.Mockito.when(hadithRepository.findById(hadith.getId())).thenReturn(java.util.Optional.of(hadith));
        org.mockito.Mockito.when(currentUserService.getCurrentUser()).thenReturn(java.util.Optional.empty());
        org.mockito.Mockito.when(hadithRepository.saveAndFlush(org.mockito.ArgumentMatchers.any())).thenReturn(hadith);

        hadithService.patchHadith(hadith.getId(), patchDto);

        assertEquals("New Valid Text", hadith.getText());
    }

    @Test
    void applyPatch_ShouldThrowException_WhenTextIsBlank() {
        HadithPatchDto patchDto = new HadithPatchDto();
        patchDto.setText(TriState.defined("  "));
        org.mockito.Mockito.when(hadithRepository.findById(hadith.getId())).thenReturn(java.util.Optional.of(hadith));

        assertThrows(InvalidRequestException.class, () -> hadithService.patchHadith(hadith.getId(), patchDto));
    }

    @Test
    void applyPatch_ShouldUpdateHadithNumber_WhenValid() {
        HadithPatchDto patchDto = new HadithPatchDto();
        patchDto.setHadithNumber(TriState.defined(123));
        org.mockito.Mockito.when(hadithRepository.findById(hadith.getId())).thenReturn(java.util.Optional.of(hadith));
        org.mockito.Mockito.when(currentUserService.getCurrentUser()).thenReturn(java.util.Optional.empty());
        org.mockito.Mockito.when(hadithRepository.saveAndFlush(org.mockito.ArgumentMatchers.any())).thenReturn(hadith);

        hadithService.patchHadith(hadith.getId(), patchDto);

        assertEquals(123, hadith.getHadithNumber());
    }

    @Test
    void applyPatch_ShouldThrowException_WhenHadithNumberIsNegative() {
        HadithPatchDto patchDto = new HadithPatchDto();
        patchDto.setHadithNumber(TriState.defined(-5));
        org.mockito.Mockito.when(hadithRepository.findById(hadith.getId())).thenReturn(java.util.Optional.of(hadith));

        assertThrows(InvalidRequestException.class, () -> hadithService.patchHadith(hadith.getId(), patchDto));
    }

    @Test
    void applyPatch_ShouldUpdateSanad_WhenDefined() {
        HadithPatchDto patchDto = new HadithPatchDto();
        patchDto.setSanad(TriState.defined("New Sanad"));
        org.mockito.Mockito.when(hadithRepository.findById(hadith.getId())).thenReturn(java.util.Optional.of(hadith));
        org.mockito.Mockito.when(currentUserService.getCurrentUser()).thenReturn(java.util.Optional.empty());
        org.mockito.Mockito.when(hadithRepository.saveAndFlush(org.mockito.ArgumentMatchers.any())).thenReturn(hadith);

        hadithService.patchHadith(hadith.getId(), patchDto);

        assertEquals("New Sanad", hadith.getSanad());
    }

    @Test
    void applyPatch_ShouldAllowNullSanad() {
        HadithPatchDto patchDto = new HadithPatchDto();
        patchDto.setSanad(TriState.defined(null));
        org.mockito.Mockito.when(hadithRepository.findById(hadith.getId())).thenReturn(java.util.Optional.of(hadith));
        org.mockito.Mockito.when(currentUserService.getCurrentUser()).thenReturn(java.util.Optional.empty());
        org.mockito.Mockito.when(hadithRepository.saveAndFlush(org.mockito.ArgumentMatchers.any())).thenReturn(hadith);

        hadithService.patchHadith(hadith.getId(), patchDto);

        assertNull(hadith.getSanad());
    }
}
