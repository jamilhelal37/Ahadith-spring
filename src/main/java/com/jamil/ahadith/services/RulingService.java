package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.RulingRequestDto;
import com.jamil.ahadith.dtos.responses.RulingResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.updates.RulingUpdateDto;
import com.jamil.ahadith.exceptions.RulingNotFoundException;
import com.jamil.ahadith.mappers.RulingMapper;
import com.jamil.ahadith.repositories.RulingRepository;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
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
        return rulingMapper.toResponseDto(ruling);
    }

    public RulingResponseDto updateRuling(UUID id, RulingUpdateDto request) {
        var ruling = rulingRepository.findById(id).orElseThrow(RulingNotFoundException::new);
        rulingMapper.updateEntity(request, ruling);
        currentUserService.getCurrentUser().ifPresent(ruling::setUpdatedBy);
        var savedRuling = rulingRepository.saveAndFlush(ruling);
        entityManager.refresh(savedRuling);
        return rulingMapper.toResponseDto(savedRuling);
    }

    public void deleteRuling(UUID id) {
        if (!rulingRepository.existsById(id)) {
            throw new RulingNotFoundException();
        }
        rulingRepository.deleteById(id);
    }

}



