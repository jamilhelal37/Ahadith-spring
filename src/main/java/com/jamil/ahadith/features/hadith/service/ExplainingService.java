package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.hadith.dto.request.ExplainingRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.ExplainingResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.update.ExplainingUpdateDto;
import com.jamil.ahadith.features.hadith.exception.ExplainingNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.ExplainingMapper;
import com.jamil.ahadith.features.hadith.repository.ExplainingRepository;
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
public class ExplainingService {
    private final ExplainingRepository explainingRepository;
    private final ExplainingMapper explainingMapper;
    private final EntityManager entityManager;
    private final AdminPageService adminPageService;
    private final CurrentUserService currentUserService;
    private final AuditEventPublisher auditEventPublisher;

    public SearchResponse<ExplainingResponseDto> getExplainings(Pageable pageable) {
        return adminPageService.response(explainingRepository.findAll(pageable).map(explainingMapper::toResponseDto));
    }

    public ExplainingResponseDto getExplainingById(UUID id) {
        return explainingRepository.findById(id)
                .map(explainingMapper::toResponseDto)
                .orElseThrow(ExplainingNotFoundException::new);
    }

    public ExplainingResponseDto createExplaining(ExplainingRequestDto request) {
        var entity = explainingMapper.toEntity(request);
        currentUserService.getCurrentUser().ifPresent(entity::setCreatedBy);
        var explaining = explainingRepository.saveAndFlush(entity);
        entityManager.refresh(explaining);
        auditEventPublisher.publishCreate("explaining", explaining.getId(), AuditData.snapshot(explaining));
        return explainingMapper.toResponseDto(explaining);
    }

    public ExplainingResponseDto updateExplaining(UUID id, ExplainingUpdateDto request) {
        var explaining = explainingRepository.findById(id).orElseThrow(ExplainingNotFoundException::new);
        var oldData = AuditData.snapshot(explaining);
        explainingMapper.updateEntity(request, explaining);
        currentUserService.getCurrentUser().ifPresent(explaining::setUpdatedBy);
        var savedExplaining = explainingRepository.saveAndFlush(explaining);
        entityManager.refresh(savedExplaining);
        auditEventPublisher.publishUpdate("explaining", savedExplaining.getId(), oldData, AuditData.snapshot(savedExplaining));
        return explainingMapper.toResponseDto(savedExplaining);
    }

    public void deleteExplaining(UUID id) {
        var explaining = explainingRepository.findById(id).orElseThrow(ExplainingNotFoundException::new);
        var oldData = AuditData.snapshot(explaining);
        explainingRepository.delete(explaining);
        auditEventPublisher.publishDelete("explaining", id, oldData);
    }
}
