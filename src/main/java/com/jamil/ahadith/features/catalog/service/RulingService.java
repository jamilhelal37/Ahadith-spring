package com.jamil.ahadith.features.catalog.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.catalog.entity.Ruling;

import com.jamil.ahadith.features.user.service.CurrentUserService;

import com.jamil.ahadith.features.catalog.dto.request.RulingRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.RulingResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.catalog.dto.update.RulingUpdateDto;
import com.jamil.ahadith.features.catalog.exception.RulingNotFoundException;
import com.jamil.ahadith.features.catalog.mapper.RulingMapper;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.search.event.ReferenceDataChangedEvent;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class RulingService {
    private final RulingRepository rulingRepository;
    private final RulingMapper rulingMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final AdminPageService adminPageService;
    private final ApplicationEventPublisher eventPublisher;

    public SearchResponse<RulingResponseDto> getRulings(Pageable pageable) {
        return adminPageService.response(rulingRepository.findAll(pageable).map(rulingMapper::toResponseDto));
    }

    public RulingResponseDto getRulingById(UUID id) {
        return rulingRepository.findById(id)
                .map(rulingMapper::toResponseDto)
                .orElseThrow(RulingNotFoundException::new);
    }

    public RulingResponseDto createRuling(RulingRequestDto request) {
        var ruling = rulingMapper.toEntity(request);
        currentUserService.getCurrentUser().ifPresent(ruling::setCreatedBy);
        ruling = rulingRepository.saveAndFlush(ruling);
        entityManager.refresh(ruling);
        publishReferenceDataChanged();
        return rulingMapper.toResponseDto(ruling);
    }

    public RulingResponseDto updateRuling(UUID id, RulingUpdateDto request) {
        var ruling = rulingRepository.findById(id).orElseThrow(RulingNotFoundException::new);
        rulingMapper.updateEntity(request, ruling);
        currentUserService.getCurrentUser().ifPresent(ruling::setUpdatedBy);
        var savedRuling = rulingRepository.saveAndFlush(ruling);
        entityManager.refresh(savedRuling);
        publishReferenceDataChanged();
        return rulingMapper.toResponseDto(savedRuling);
    }

    public void deleteRuling(UUID id) {
        if (!rulingRepository.existsById(id)) {
            throw new RulingNotFoundException();
        }
        rulingRepository.deleteById(id);
        publishReferenceDataChanged();
    }

    private void publishReferenceDataChanged() {
        eventPublisher.publishEvent(new ReferenceDataChangedEvent());
    }

}



