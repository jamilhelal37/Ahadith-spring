package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.exception.ConflictException;
import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.hadith.dto.request.SimilarAhadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.SimilarAhadithResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.update.SimilarAhadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.exception.SimilarAhadithNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.SimilarAhadithMapper;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.hadith.repository.SimilarAhadithRepository;
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
public class SimilarAhadithService {
    private final SimilarAhadithRepository similarAhadithRepository;
    private final SimilarAhadithMapper similarAhadithMapper;
    private final EntityManager entityManager;
    private final AdminPageService adminPageService;
    private final HadithRepository hadithRepository;
    private final CurrentUserService currentUserService;
    private final AuditEventPublisher auditEventPublisher;

    public SearchResponse<SimilarAhadithResponseDto> getSimilarAhadiths(Pageable pageable) {
        return adminPageService.response(similarAhadithRepository.findAll(pageable).map(similarAhadithMapper::toResponseDto));
    }

    public SimilarAhadithResponseDto getSimilarAhadithById(UUID id) {
        return similarAhadithRepository.findById(id)
                .map(similarAhadithMapper::toResponseDto)
                .orElseThrow(SimilarAhadithNotFoundException::new);
    }

    public SimilarAhadithResponseDto createSimilarAhadith(SimilarAhadithRequestDto request) {
        var similarAhadith = similarAhadithMapper.toEntity(request);
        var mainHadith = resolveHadith(request.getMainHadith());
        var simHadith = resolveHadith(request.getSimHadith());
        validateRelationship(mainHadith.getId(), simHadith.getId(), null);
        similarAhadith.setMainHadith(mainHadith);
        similarAhadith.setSimHadith(simHadith);
        currentUserService.getCurrentUser().ifPresent(similarAhadith::setCreatedBy);
        var entity = similarAhadithRepository.saveAndFlush(similarAhadith);
        entityManager.refresh(entity);
        auditEventPublisher.publishCreate("similar_ahadith", entity.getId(), AuditData.snapshot(entity));
        return similarAhadithMapper.toResponseDto(entity);
    }

    public SimilarAhadithResponseDto updateSimilarAhadith(UUID id, SimilarAhadithUpdateDto request) {
        var entity = similarAhadithRepository.findById(id).orElseThrow(SimilarAhadithNotFoundException::new);
        var oldData = AuditData.snapshot(entity);
        similarAhadithMapper.updateEntity(request, entity);
        if (request.getMainHadith() != null) {
            entity.setMainHadith(resolveHadith(request.getMainHadith()));
        }
        if (request.getSimHadith() != null) {
            entity.setSimHadith(resolveHadith(request.getSimHadith()));
        }
        validateRelationship(entity.getMainHadith().getId(), entity.getSimHadith().getId(), id);
        currentUserService.getCurrentUser().ifPresent(entity::setUpdatedBy);
        var saved = similarAhadithRepository.saveAndFlush(entity);
        entityManager.refresh(saved);
        auditEventPublisher.publishUpdate("similar_ahadith", saved.getId(), oldData, AuditData.snapshot(saved));
        return similarAhadithMapper.toResponseDto(saved);
    }

    public void deleteSimilarAhadith(UUID id) {
        var similar = similarAhadithRepository.findById(id).orElseThrow(SimilarAhadithNotFoundException::new);
        var oldData = AuditData.snapshot(similar);
        similarAhadithRepository.delete(similar);
        auditEventPublisher.publishDelete("similar_ahadith", id, oldData);
    }

    private Hadith resolveHadith(HadithReferenceRequestDto reference) {
        return hadithRepository.findById(reference.getId())
                .orElseThrow(HadithNotFoundException::new);
    }

    private void validateRelationship(UUID mainHadithId, UUID simHadithId, UUID existingId) {
        if (mainHadithId.equals(simHadithId)) {
            throw new InvalidRequestException("A hadith cannot be linked as similar to itself.");
        }
        boolean duplicate = existingId == null
                ? similarAhadithRepository.existsByMainHadithIdAndSimHadithId(mainHadithId, simHadithId)
                : similarAhadithRepository.existsByMainHadithIdAndSimHadithIdAndIdNot(mainHadithId, simHadithId, existingId);
        if (duplicate) {
            throw new ConflictException("Similar hadith relationship already exists.");
        }
    }
}
