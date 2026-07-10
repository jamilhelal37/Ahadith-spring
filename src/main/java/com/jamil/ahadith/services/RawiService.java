package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.RawiRequestDto;
import com.jamil.ahadith.dtos.responses.RawiResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.updates.RawiUpdateDto;
import com.jamil.ahadith.exceptions.RawiNotFoundException;
import com.jamil.ahadith.mappers.RawiMapper;
import com.jamil.ahadith.repositories.RawiRepository;
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
public class RawiService {
    private final RawiRepository rawiRepository;
    private final RawiMapper rawiMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final AdminPageService adminPageService;

    public SearchResponse<RawiResponseDto> getRawis(Pageable pageable) {
        return adminPageService.response(rawiRepository.findAll(pageable).map(rawiMapper::toResponseDto));
    }

    public RawiResponseDto getRawiById(UUID id) {
        return rawiRepository.findById(id)
                .map(rawiMapper::toResponseDto)
                .orElseThrow(RawiNotFoundException::new);
    }

    public RawiResponseDto createRawi(RawiRequestDto request) {
        var rawi = rawiMapper.toEntity(request);
        currentUserService.getCurrentUser().ifPresent(rawi::setCreatedBy);
        rawi = rawiRepository.saveAndFlush(rawi);
        entityManager.refresh(rawi);
        return rawiMapper.toResponseDto(rawi);
    }

    public RawiResponseDto updateRawi(UUID id, RawiUpdateDto request) {
        var rawi = rawiRepository.findById(id).orElseThrow(RawiNotFoundException::new);
        rawiMapper.updateEntity(request, rawi);
        currentUserService.getCurrentUser().ifPresent(rawi::setUpdatedBy);
        var savedRawi = rawiRepository.saveAndFlush(rawi);
        entityManager.refresh(savedRawi);
        return rawiMapper.toResponseDto(savedRawi);
    }

    public void deleteRawi(UUID id) {
        if (!rawiRepository.existsById(id)) {
            throw new RawiNotFoundException();
        }
        rawiRepository.deleteById(id);
    }

}



