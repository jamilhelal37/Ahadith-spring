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
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.update.SimilarAhadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.entity.SimilarAhadith;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.exception.SimilarAhadithNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.HadithReferenceResponseMapper;
import com.jamil.ahadith.features.hadith.mapper.SimilarAhadithMapper;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.hadith.repository.SimilarAhadithRepository;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final HadithSearchService hadithSearchService;

    public SearchResponse<SimilarAhadithResponseDto> getSimilarAhadiths(Pageable pageable) {
        Page<SimilarAhadith> page =
                similarAhadithRepository.findAll(pageable);
        Map<UUID, HadithReferenceResponseDto> hadithMap =
                loadHadithReferences(page.getContent());

        return adminPageService.response(
                page.map(similarAhadith ->
                        toResponseWithFullHadithReferences(
                                similarAhadith,
                                hadithMap
                        )
                )
        );
    }

    public SimilarAhadithResponseDto getSimilarAhadithById(UUID id) {
        return similarAhadithRepository.findById(id)
                .map(this::toResponseWithFullHadithReferences)
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
        return toResponseWithFullHadithReferences(entity);
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
        return toResponseWithFullHadithReferences(saved);
    }

    private SimilarAhadithResponseDto toResponseWithFullHadithReferences(
            SimilarAhadith similarAhadith) {

        return toResponseWithFullHadithReferences(
                similarAhadith,
                loadHadithReferences(List.of(similarAhadith))
        );
    }

    private SimilarAhadithResponseDto toResponseWithFullHadithReferences(
            SimilarAhadith similarAhadith,
            Map<UUID, HadithReferenceResponseDto> hadithMap) {

        var dto = similarAhadithMapper.toResponseDto(similarAhadith);

        if (similarAhadith.getMainHadith() == null) {
            dto.setMainHadith(null);
        } else {
            dto.setMainHadith(
                    hadithMap.get(similarAhadith.getMainHadith().getId())
            );
        }

        if (similarAhadith.getSimHadith() == null) {
            dto.setSimHadith(null);
        } else {
            dto.setSimHadith(
                    hadithMap.get(similarAhadith.getSimHadith().getId())
            );
        }

        return dto;
    }

    private Map<UUID, HadithReferenceResponseDto> loadHadithReferences(
            List<SimilarAhadith> similarAhadiths) {

        List<UUID> ids = similarAhadiths.stream()
                .flatMap(similarAhadith -> Stream.of(
                        similarAhadith.getMainHadith(),
                        similarAhadith.getSimHadith()
                ))
                .filter(Objects::nonNull)
                .map(Hadith::getId)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            return Map.of();
        }

        return hadithSearchService
                .getHadithCardsByIdsInOrder(ids)
                .stream()
                .map(HadithReferenceResponseMapper::fromSearchItem)
                .collect(Collectors.toMap(
                        HadithReferenceResponseDto::getId,
                        Function.identity()
                ));
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
