package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.hadith.dto.request.SimilarAhadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.SimilarAhadithResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.update.SimilarAhadithUpdateDto;
import com.jamil.ahadith.features.hadith.exception.SimilarAhadithNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.SimilarAhadithMapper;
import com.jamil.ahadith.features.hadith.repository.SimilarAhadithRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class SimilarAhadithService {
    private final SimilarAhadithRepository similarAhadithRepository;
    private final SimilarAhadithMapper similarAhadithMapper;
    private final EntityManager entityManager;
    private final AdminPageService adminPageService;

    public SearchResponse<SimilarAhadithResponseDto> getSimilarAhadiths(Pageable pageable) {
        return adminPageService.response(similarAhadithRepository.findAll(pageable).map(similarAhadithMapper::toResponseDto));
    }

    public SimilarAhadithResponseDto getSimilarAhadithById(UUID id) {
        return similarAhadithRepository.findById(id)
                .map(similarAhadithMapper::toResponseDto)
                .orElseThrow(SimilarAhadithNotFoundException::new);
    }

    public SimilarAhadithResponseDto createSimilarAhadith(SimilarAhadithRequestDto request) {
        var entity = similarAhadithRepository.saveAndFlush(similarAhadithMapper.toEntity(request));
        entityManager.refresh(entity);
        return similarAhadithMapper.toResponseDto(entity);
    }

    public SimilarAhadithResponseDto updateSimilarAhadith(UUID id, SimilarAhadithUpdateDto request) {
        var entity = similarAhadithRepository.findById(id).orElseThrow(SimilarAhadithNotFoundException::new);
        similarAhadithMapper.updateEntity(request, entity);
        var saved = similarAhadithRepository.saveAndFlush(entity);
        entityManager.refresh(saved);
        return similarAhadithMapper.toResponseDto(saved);
    }

    public void deleteSimilarAhadith(UUID id) {
        if (!similarAhadithRepository.existsById(id)) {
            throw new SimilarAhadithNotFoundException();
        }
        similarAhadithRepository.deleteById(id);
    }
}
