package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.SimilarAhadithRequestDto;
import com.jamil.ahadith.dtos.responses.SimilarAhadithResponseDto;
import com.jamil.ahadith.dtos.updates.SimilarAhadithUpdateDto;
import com.jamil.ahadith.exceptions.SimilarAhadithNotFoundException;
import com.jamil.ahadith.mappers.SimilarAhadithMapper;
import com.jamil.ahadith.repositories.SimilarAhadithRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class SimilarAhadithService {
    private final SimilarAhadithRepository similarAhadithRepository;
    private final SimilarAhadithMapper similarAhadithMapper;
    private final EntityManager entityManager;

    public List<SimilarAhadithResponseDto> getSimilarAhadiths() {
        return similarAhadithRepository.findAll().stream()
                .map(similarAhadithMapper::toResponseDto)
                .toList();
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
