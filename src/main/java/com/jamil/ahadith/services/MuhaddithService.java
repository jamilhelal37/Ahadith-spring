package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.MuhaddithRequestDto;
import com.jamil.ahadith.dtos.responses.MuhaddithResponseDto;
import com.jamil.ahadith.dtos.updates.MuhaddithUpdateDto;
import com.jamil.ahadith.exceptions.MuhaddithNotFoundException;
import com.jamil.ahadith.mappers.MuhaddithMapper;
import com.jamil.ahadith.repositories.MuhaddithRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class MuhaddithService {
    private final MuhaddithRepository muhaddithRepository;
    private final MuhaddithMapper muhaddithMapper;
    private final EntityManager entityManager;

    public List<MuhaddithResponseDto> getMuhaddiths() {
        return muhaddithRepository.findAll().stream()
                .map(muhaddithMapper::toResponseDto)
                .toList();
    }

    public MuhaddithResponseDto getMuhaddithById(UUID id) {
        return muhaddithRepository.findById(id)
                .map(muhaddithMapper::toResponseDto)
                .orElseThrow(MuhaddithNotFoundException::new);
    }

    public MuhaddithResponseDto createMuhaddith(MuhaddithRequestDto request) {
        var muhaddith = muhaddithRepository.saveAndFlush(muhaddithMapper.toEntity(request));
        entityManager.refresh(muhaddith);
        return muhaddithMapper.toResponseDto(muhaddith);
    }

    public MuhaddithResponseDto updateMuhaddith(UUID id, MuhaddithUpdateDto request) {
        var muhaddith = muhaddithRepository.findById(id).orElseThrow(MuhaddithNotFoundException::new);
        muhaddithMapper.updateEntity(request, muhaddith);
        var savedMuhaddith = muhaddithRepository.saveAndFlush(muhaddith);
        entityManager.refresh(savedMuhaddith);
        return muhaddithMapper.toResponseDto(savedMuhaddith);
    }

    public void deleteMuhaddith(UUID id) {
        if (!muhaddithRepository.existsById(id)) {
            throw new MuhaddithNotFoundException();
        }
        muhaddithRepository.deleteById(id);
    }
}