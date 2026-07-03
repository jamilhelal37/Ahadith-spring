package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.UpgradeRequestDto;
import com.jamil.ahadith.dtos.responses.UpgradeRequestResponseDto;
import com.jamil.ahadith.entities.UpgradeRequest;
import com.jamil.ahadith.exceptions.UpgradeRequestNotFoundException;
import com.jamil.ahadith.mappers.UpgradeRequestMapper;
import com.jamil.ahadith.repositories.UpgradeRequestRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class UpgradeRequestService {
    private final UpgradeRequestRepository upgradeRequestRepository;
    private final UpgradeRequestMapper upgradeRequestMapper;
    private final EntityManager entityManager;

    public List<UpgradeRequestResponseDto> getUpgradeRequests() {
        return upgradeRequestRepository.findAll().stream()
                .map(upgradeRequestMapper::toResponseDto)
                .toList();
    }

    public UpgradeRequestResponseDto getUpgradeRequestById(UUID id) {
        return upgradeRequestRepository.findById(id)
                .map(upgradeRequestMapper::toResponseDto)
                .orElseThrow(UpgradeRequestNotFoundException::new);
    }

    public UpgradeRequestResponseDto createUpgradeRequest(UpgradeRequestDto request) {
        UpgradeRequest upgradeRequest = upgradeRequestMapper.toEntity(request);
        var saved = upgradeRequestRepository.saveAndFlush(upgradeRequest);
        entityManager.refresh(saved);
        return upgradeRequestMapper.toResponseDto(saved);
    }

    public UpgradeRequestResponseDto updateUpgradeRequest(UUID id, UpgradeRequestDto request) {
        var upgradeRequest = upgradeRequestRepository.findById(id)
                .orElseThrow(UpgradeRequestNotFoundException::new);
        upgradeRequestMapper.updateEntity(request, upgradeRequest);
        var saved = upgradeRequestRepository.saveAndFlush(upgradeRequest);
        entityManager.refresh(saved);
        return upgradeRequestMapper.toResponseDto(saved);
    }

    public void deleteUpgradeRequest(UUID id) {
        if (!upgradeRequestRepository.existsById(id)) {
            throw new UpgradeRequestNotFoundException();
        }
        upgradeRequestRepository.deleteById(id);
    }
}
