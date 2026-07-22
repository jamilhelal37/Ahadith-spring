package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import com.jamil.ahadith.features.catalog.exception.RulingNotFoundException;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.dto.request.FakeHadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.FakeHadithResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.update.FakeHadithUpdateDto;
import com.jamil.ahadith.features.hadith.exception.FakeHadithNotFoundException;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.FakeHadithMapper;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class FakeHadithService {
    private final FakeHadithRepository fakeHadithRepository;
    private final FakeHadithMapper fakeHadithMapper;
    private final EntityManager entityManager;
    private final AdminPageService adminPageService;
    private final HadithRepository hadithRepository;
    private final RulingRepository rulingRepository;
    private final CurrentUserService currentUserService;
    private final AuditEventPublisher auditEventPublisher;

    public SearchResponse<FakeHadithResponseDto> getFakeAhadith(Pageable pageable) {
        return adminPageService.response(fakeHadithRepository.findAll(pageable).map(fakeHadithMapper::toResponseDto));
    }

    public FakeHadithResponseDto getFakeHadithById(UUID id) {
        return fakeHadithRepository.findById(id)
                .map(fakeHadithMapper::toResponseDto)
                .orElseThrow(FakeHadithNotFoundException::new);
    }

    public FakeHadithResponseDto createFakeHadith(FakeHadithRequestDto request) {
        var entity = fakeHadithMapper.toEntity(request);
        applyCreateRelations(request, entity);
        currentUserService.getCurrentUser().ifPresent(entity::setCreatedBy);
        var fakeHadith = fakeHadithRepository.saveAndFlush(entity);
        entityManager.refresh(fakeHadith);
        auditEventPublisher.publishCreate("fake_ahadith", fakeHadith.getId(), AuditData.snapshot(fakeHadith));
        return fakeHadithMapper.toResponseDto(fakeHadith);
    }

    public FakeHadithResponseDto updateFakeHadith(UUID id, FakeHadithUpdateDto request) {
        var fakeHadith = fakeHadithRepository.findById(id).orElseThrow(FakeHadithNotFoundException::new);
        var oldData = AuditData.snapshot(fakeHadith);
        fakeHadithMapper.updateEntity(request, fakeHadith);
        applyUpdateRelations(request, fakeHadith);
        currentUserService.getCurrentUser().ifPresent(fakeHadith::setUpdatedBy);
        var savedFakeHadith = fakeHadithRepository.saveAndFlush(fakeHadith);
        entityManager.refresh(savedFakeHadith);
        auditEventPublisher.publishUpdate("fake_ahadith", savedFakeHadith.getId(), oldData, AuditData.snapshot(savedFakeHadith));
        return fakeHadithMapper.toResponseDto(savedFakeHadith);
    }

    public void deleteFakeHadith(UUID id) {
        var fakeHadith = fakeHadithRepository.findById(id).orElseThrow(FakeHadithNotFoundException::new);
        var oldData = AuditData.snapshot(fakeHadith);
        fakeHadithRepository.delete(fakeHadith);
        auditEventPublisher.publishDelete("fake_ahadith", id, oldData);
    }

    private void applyCreateRelations(FakeHadithRequestDto request, FakeHadith entity) {
        entity.setSubValid(resolveHadith(request.getSubValid()));
        entity.setRuling(resolveRuling(request.getRuling()));
    }

    private void applyUpdateRelations(FakeHadithUpdateDto request, FakeHadith entity) {
        if (request.getSubValid() != null) {
            entity.setSubValid(resolveHadith(request.getSubValid()));
        }
        if (request.getRuling() != null) {
            entity.setRuling(resolveRuling(request.getRuling()));
        }
    }

    private Hadith resolveHadith(HadithReferenceRequestDto reference) {
        if (reference == null) {
            return null;
        }
        return hadithRepository.findById(reference.getId())
                .orElseThrow(HadithNotFoundException::new);
    }

    private Ruling resolveRuling(com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto reference) {
        if (reference == null) {
            return null;
        }
        return rulingRepository.findById(reference.getId())
                .orElseThrow(RulingNotFoundException::new);
    }
}
