package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.hadith.entity.Explaining;

import com.jamil.ahadith.features.hadith.dto.request.ExplainingRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.ExplainingResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.update.ExplainingUpdateDto;
import com.jamil.ahadith.features.hadith.exception.ExplainingNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.ExplainingMapper;
import com.jamil.ahadith.features.hadith.repository.ExplainingRepository;
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
public class ExplainingService {
    private final ExplainingRepository explainingRepository;
    private final ExplainingMapper explainingMapper;
    private final EntityManager entityManager;
    private final AdminPageService adminPageService;

    public SearchResponse<ExplainingResponseDto> getExplainings(Pageable pageable) {
        return adminPageService.response(explainingRepository.findAll(pageable).map(explainingMapper::toResponseDto));
    }

    public ExplainingResponseDto getExplainingById(UUID id) {
        return explainingRepository.findById(id)
                .map(explainingMapper::toResponseDto)
                .orElseThrow(ExplainingNotFoundException::new);
    }

    public ExplainingResponseDto createExplaining(ExplainingRequestDto request) {
        var explaining = explainingRepository.saveAndFlush(explainingMapper.toEntity(request));
        entityManager.refresh(explaining);
        return explainingMapper.toResponseDto(explaining);
    }

    public ExplainingResponseDto updateExplaining(UUID id, ExplainingUpdateDto request) {
        var explaining = explainingRepository.findById(id).orElseThrow(ExplainingNotFoundException::new);
        explainingMapper.updateEntity(request, explaining);
        var savedExplaining = explainingRepository.saveAndFlush(explaining);
        entityManager.refresh(savedExplaining);
        return explainingMapper.toResponseDto(savedExplaining);
    }

    public void deleteExplaining(UUID id) {
        if (!explainingRepository.existsById(id)) {
            throw new ExplainingNotFoundException();
        }
        explainingRepository.deleteById(id);
    }
}
