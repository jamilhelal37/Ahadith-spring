package com.jamil.ahadith.features.catalog.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;

import com.jamil.ahadith.features.user.service.CurrentUserService;

import com.jamil.ahadith.features.catalog.dto.request.MuhaddithRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.MuhaddithResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.catalog.dto.update.MuhaddithUpdateDto;
import com.jamil.ahadith.features.catalog.exception.MuhaddithNotFoundException;
import com.jamil.ahadith.features.catalog.mapper.MuhaddithMapper;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.search.service.SearchFiltersService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class MuhaddithService {
    private final MuhaddithRepository muhaddithRepository;
    private final MuhaddithMapper muhaddithMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final AdminPageService adminPageService;
    private final SearchFiltersService searchFiltersService;

    public SearchResponse<MuhaddithResponseDto> getMuhaddiths(Pageable pageable) {
        return adminPageService.response(muhaddithRepository.findAll(pageable).map(muhaddithMapper::toResponseDto));
    }

    public MuhaddithResponseDto getMuhaddithById(UUID id) {
        return muhaddithRepository.findById(id)
                .map(muhaddithMapper::toResponseDto)
                .orElseThrow(MuhaddithNotFoundException::new);
    }

    public MuhaddithResponseDto createMuhaddith(MuhaddithRequestDto request) {
        var muhaddith = muhaddithMapper.toEntity(request);
        currentUserService.getCurrentUser().ifPresent(muhaddith::setCreatedBy);
        muhaddith = muhaddithRepository.saveAndFlush(muhaddith);
        entityManager.refresh(muhaddith);
        searchFiltersService.evictReferenceCaches();
        return muhaddithMapper.toResponseDto(muhaddith);
    }

    public MuhaddithResponseDto updateMuhaddith(UUID id, MuhaddithUpdateDto request) {
        var muhaddith = muhaddithRepository.findById(id).orElseThrow(MuhaddithNotFoundException::new);
        muhaddithMapper.updateEntity(request, muhaddith);
        currentUserService.getCurrentUser().ifPresent(muhaddith::setUpdatedBy);
        var savedMuhaddith = muhaddithRepository.saveAndFlush(muhaddith);
        entityManager.refresh(savedMuhaddith);
        searchFiltersService.evictReferenceCaches();
        return muhaddithMapper.toResponseDto(savedMuhaddith);
    }

    public void deleteMuhaddith(UUID id) {
        if (!muhaddithRepository.existsById(id)) {
            throw new MuhaddithNotFoundException();
        }
        muhaddithRepository.deleteById(id);
        searchFiltersService.evictReferenceCaches();
    }
}
