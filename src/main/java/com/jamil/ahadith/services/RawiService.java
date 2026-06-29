package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.RulingRequestDto;
import com.jamil.ahadith.dtos.responses.RulingResponseDto;
import com.jamil.ahadith.dtos.updates.RulingUpdateDto;
import com.jamil.ahadith.exceptions.RulingNotFoundException;
import com.jamil.ahadith.mappers.RulingMapper;
import com.jamil.ahadith.repositories.RulingRepository;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class RulingService {
    private final RulingRepository rulingRepository;
    private final RulingMapper rulingMapper;
    private final EntityManager entityManager;

    public List<RulingResponseDto> getRulings() {
        return rulingRepository.findAll().stream()
                .map(rulingMapper::toResponseDto)
                .toList();
    }

    public RulingResponseDto getRulingById(UUID id) {
        return rulingRepository.findById(id)
                .map(rulingMapper::toResponseDto)
                .orElseThrow(RulingNotFoundException::new);
    }

    public RulingResponseDto createRuling(RulingRequestDto request) {
        var ruling = rulingRepository.saveAndFlush(rulingMapper.toEntity(request));
        entityManager.refresh(ruling);
        return rulingMapper.toResponseDto(ruling);
    }

    public RulingResponseDto updateRuling(UUID id, RulingUpdateDto request) {
        var ruling = rulingRepository.findById(id).orElseThrow(RulingNotFoundException::new);
        rulingMapper.updateEntity(request, ruling);
        var savedRuling = rulingRepository.saveAndFlush(ruling);
        entityManager.refresh(savedRuling);
        return rulingMapper.toResponseDto(savedRuling);
    }

}



