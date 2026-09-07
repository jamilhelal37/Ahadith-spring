package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import com.jamil.ahadith.features.catalog.exception.RulingNotFoundException;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.hadith.dto.request.FakeHadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.FakeHadithResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.update.FakeHadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.event.FakeHadithCreatedEvent;
import com.jamil.ahadith.features.hadith.exception.FakeHadithNotFoundException;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.FakeHadithMapper;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.features.search.entity.SearchSource;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import com.jamil.ahadith.features.search.service.SearchHistoryService;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final SearchHistoryService searchHistoryService;
    private final HadithSearchService hadithSearchService;

    public SearchResponse<FakeHadithResponseDto> getFakeAhadith(Pageable pageable) {
        var page = fakeHadithRepository.findAll(pageable);
        return toSearchResponse(page);
    }

    public SearchResponse<FakeHadithResponseDto> getFakeAhadith(
            String query,
            Pageable pageable) {

        String searchText = query == null ? null : query.trim();

        if (searchText == null || searchText.isBlank()) {
            var page = fakeHadithRepository.findAll(pageable);
            return toSearchResponse(page);
        }

        var page = fakeHadithRepository.searchByText(searchText, pageable);

        searchHistoryService.saveCurrentUserSearch(
                searchText,
                SearchSource.fake_hadith
        );

        return toSearchResponse(page);
    }

    public FakeHadithResponseDto getFakeHadithById(UUID id) {
        var fakeHadith = fakeHadithRepository.findById(id)
                .orElseThrow(FakeHadithNotFoundException::new);

        return toResponseWithFullSubValid(fakeHadith);
    }

    public FakeHadithResponseDto createFakeHadith(FakeHadithRequestDto request) {
        var entity = fakeHadithMapper.toEntity(request);

        applyCreateRelations(request, entity);

        currentUserService.getCurrentUser()
                .ifPresent(entity::setCreatedBy);

        var fakeHadith = fakeHadithRepository.saveAndFlush(entity);

        entityManager.refresh(fakeHadith);

        eventPublisher.publishEvent(
                new FakeHadithCreatedEvent(
                        fakeHadith.getId(),
                        fakeHadith.getText()
                )
        );

        auditEventPublisher.publishCreate(
                "fake_ahadith",
                fakeHadith.getId(),
                AuditData.snapshot(fakeHadith)
        );

        return toResponseWithFullSubValid(fakeHadith);
    }

    public FakeHadithResponseDto updateFakeHadith(
            UUID id,
            FakeHadithUpdateDto request) {

        requireAtLeastOneUpdateField(request);

        var fakeHadith = fakeHadithRepository.findById(id)
                .orElseThrow(FakeHadithNotFoundException::new);

        var oldData = AuditData.snapshot(fakeHadith);

        fakeHadithMapper.updateEntity(request, fakeHadith);

        applyUpdateRelations(request, fakeHadith);

        currentUserService.getCurrentUser()
                .ifPresent(fakeHadith::setUpdatedBy);

        var savedFakeHadith =
                fakeHadithRepository.saveAndFlush(fakeHadith);

        entityManager.refresh(savedFakeHadith);

        auditEventPublisher.publishUpdate(
                "fake_ahadith",
                savedFakeHadith.getId(),
                oldData,
                AuditData.snapshot(savedFakeHadith)
        );

        return toResponseWithFullSubValid(savedFakeHadith);
    }

    public void deleteFakeHadith(UUID id) {
        var fakeHadith = fakeHadithRepository.findById(id)
                .orElseThrow(FakeHadithNotFoundException::new);

        var oldData = AuditData.snapshot(fakeHadith);

        fakeHadithRepository.delete(fakeHadith);

        auditEventPublisher.publishDelete(
                "fake_ahadith",
                id,
                oldData
        );
    }

    private SearchResponse<FakeHadithResponseDto> toSearchResponse(
            Page<FakeHadith> page) {

        Map<UUID, HadithReferenceResponseDto> subValidMap =
                loadSubValidDetails(page.getContent());

        var responsePage = page.map(fakeHadith -> {
            var dto = fakeHadithMapper.toResponseDto(fakeHadith);

            if (fakeHadith.getSubValid() != null) {
                dto.setSubValid(
                        subValidMap.get(
                                fakeHadith.getSubValid().getId()
                        )
                );
            }

            return dto;
        });

        return adminPageService.response(responsePage);
    }

    private FakeHadithResponseDto toResponseWithFullSubValid(
            FakeHadith fakeHadith) {

        var dto = fakeHadithMapper.toResponseDto(fakeHadith);

        if (fakeHadith.getSubValid() == null) {
            dto.setSubValid(null);
            return dto;
        }

        var subValid = hadithSearchService
                .getHadithCardsByIdsInOrder(
                        List.of(fakeHadith.getSubValid().getId())
                )
                .stream()
                .findFirst()
                .map(this::toHadithReference)
                .orElse(null);

        dto.setSubValid(subValid);

        return dto;
    }

    private Map<UUID, HadithReferenceResponseDto> loadSubValidDetails(
            List<FakeHadith> fakeAhadiths) {

        List<UUID> ids = fakeAhadiths.stream()
                .map(FakeHadith::getSubValid)
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
                .map(this::toHadithReference)
                .collect(Collectors.toMap(
                        HadithReferenceResponseDto::getId,
                        Function.identity()
                ));
    }

    private HadithReferenceResponseDto toHadithReference(
            HadithSearchItemDto item) {

        return new HadithReferenceResponseDto(
                item.getId(),
                item.getText(),
                item.getNormalText(),
                item.getHadithNumber(),
                item.getType(),
                item.getSanad(),
                item.getBook(),
                item.getRawi(),
                item.getRuling(),
                item.getMuhaddith(),
                item.getTopics(),
                item.isHasExplanation(),
                item.isHasSubValid()
        );
    }

    private void requireAtLeastOneUpdateField(
            FakeHadithUpdateDto request) {

        boolean anyProvided =
                request.getSubValid() != null ||
                        request.getText() != null ||
                        request.getRuling() != null;

        if (!anyProvided) {
            throw new InvalidRequestException(
                    "At least one field must be provided"
            );
        }
    }

    private void applyCreateRelations(
            FakeHadithRequestDto request,
            FakeHadith entity) {

        entity.setSubValid(
                resolveHadith(request.getSubValid())
        );

        entity.setRuling(
                resolveRuling(request.getRuling())
        );
    }

    private void applyUpdateRelations(
            FakeHadithUpdateDto request,
            FakeHadith entity) {

        if (request.getSubValid() != null) {
            entity.setSubValid(
                    resolveHadith(request.getSubValid())
            );
        }

        if (request.getRuling() != null) {
            entity.setRuling(
                    resolveRuling(request.getRuling())
            );
        }
    }

    private Hadith resolveHadith(
            HadithReferenceRequestDto reference) {

        if (reference == null) {
            return null;
        }

        return hadithRepository.findById(reference.getId())
                .orElseThrow(HadithNotFoundException::new);
    }

    private Ruling resolveRuling(
            com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto reference) {

        if (reference == null) {
            return null;
        }

        return rulingRepository.findById(reference.getId())
                .orElseThrow(RulingNotFoundException::new);
    }
}