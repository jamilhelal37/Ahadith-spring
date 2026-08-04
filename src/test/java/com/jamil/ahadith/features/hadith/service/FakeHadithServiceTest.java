package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.hadith.dto.update.FakeHadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.mapper.FakeHadithMapper;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.user.service.CurrentUserService;
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
class FakeHadithServiceTest {

    @Mock
    private FakeHadithRepository fakeHadithRepository;
    @Mock
    private FakeHadithMapper fakeHadithMapper;
    @Mock
    private EntityManager entityManager;
    @Mock
    private HadithRepository hadithRepository;
    @Mock
    private RulingRepository rulingRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private AuditEventPublisher auditEventPublisher;

    @InjectMocks
    private FakeHadithService fakeHadithService;

    private FakeHadith fakeHadith;
    private UUID fakeHadithId;

    @BeforeEach
    void setUp() {
        fakeHadithId = UUID.randomUUID();
        fakeHadith = new FakeHadith();
        fakeHadith.setId(fakeHadithId);
        fakeHadith.setText("Original Fake Hadith Text which is long");
        
        Hadith subValid = new Hadith();
        subValid.setId(UUID.randomUUID());
        fakeHadith.setSubValid(subValid);
        
        Ruling ruling = new Ruling();
        ruling.setId(UUID.randomUUID());
        fakeHadith.setRuling(ruling);
    }

    @Test
    void updateFakeHadith_ShouldThrowException_WhenNoFieldsProvided() {
        FakeHadithUpdateDto updateDto = new FakeHadithUpdateDto();
        assertThrows(InvalidRequestException.class, () -> fakeHadithService.updateFakeHadith(fakeHadithId, updateDto));
    }

    @Test
    void updateFakeHadith_ShouldThrowException_WhenAllFieldsAreNull() {
        FakeHadithUpdateDto updateDto = new FakeHadithUpdateDto();
        updateDto.setText(null);
        updateDto.setRuling(null);
        updateDto.setSubValid(null);
        assertThrows(InvalidRequestException.class, () -> fakeHadithService.updateFakeHadith(fakeHadithId, updateDto));
    }

    @Test
    void updateFakeHadith_ShouldUpdatePartially_WhenTextProvided() {
        FakeHadithUpdateDto updateDto = new FakeHadithUpdateDto();
        updateDto.setText("New Updated Fake Text which is also long");

        when(fakeHadithRepository.findById(fakeHadithId)).thenReturn(Optional.of(fakeHadith));
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());
        when(fakeHadithRepository.saveAndFlush(any())).thenReturn(fakeHadith);

        fakeHadithService.updateFakeHadith(fakeHadithId, updateDto);

        verify(fakeHadithMapper).updateEntity(updateDto, fakeHadith);
        verify(fakeHadithRepository).saveAndFlush(fakeHadith);
    }

    @Test
    void updateFakeHadith_ShouldNotClearOldData_WhenFieldsAreNull() {
        FakeHadithUpdateDto updateDto = new FakeHadithUpdateDto();
        updateDto.setText("New Text");
        // ruling and subValid are null in DTO

        when(fakeHadithRepository.findById(fakeHadithId)).thenReturn(Optional.of(fakeHadith));
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());
        when(fakeHadithRepository.saveAndFlush(any())).thenReturn(fakeHadith);

        fakeHadithService.updateFakeHadith(fakeHadithId, updateDto);

        // Mapping is mocked, but we should verify that relations are not set to null if they were not in the request
        // Actually, since it's mocked, we rely on the implementation calling resolve only when not null
        verify(fakeHadithMapper).updateEntity(updateDto, fakeHadith);
        
        // In the real service, applyUpdateRelations checks for null
        // Since we are unit testing the service, we check that it doesn't call resolve* for null fields
        verify(hadithRepository, never()).findById(any());
        verify(rulingRepository, never()).findById(any());
    }
}
