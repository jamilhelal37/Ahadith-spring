package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.HadithRequestDto;
import com.jamil.ahadith.dtos.responses.HadithResponseDto;
import com.jamil.ahadith.dtos.updates.HadithUpdateDto;
import com.jamil.ahadith.exceptions.HadithNotFoundException;
import com.jamil.ahadith.mappers.HadithMapper;
import com.jamil.ahadith.repositories.HadithRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class HadithService {
    private final HadithRepository hadithRepository;
    private final HadithMapper hadithMapper;
    private final EntityManager entityManager;

    public List<HadithResponseDto> getAhadith() {
        return hadithRepository.findAll().stream()
                .map(hadithMapper::toResponseDto)
                .toList();
    }

    public HadithResponseDto getHadithById(UUID id) {
        return hadithRepository.findById(id)
                .map(hadithMapper::toResponseDto)
                .orElseThrow(HadithNotFoundException::new);
    }

    public HadithResponseDto createHadith(HadithRequestDto request) {
        var hadith = hadithRepository.saveAndFlush(hadithMapper.toEntity(request));
        entityManager.refresh(hadith);
        return hadithMapper.toResponseDto(hadith);
    }

    public HadithResponseDto updateHadith(UUID id, HadithUpdateDto request) {
        var hadith = hadithRepository.findById(id).orElseThrow(HadithNotFoundException::new);
        hadithMapper.updateEntity(request, hadith);
        var savedHadith = hadithRepository.saveAndFlush(hadith);
        entityManager.refresh(savedHadith);
        return hadithMapper.toResponseDto(savedHadith);
    }

    public void deleteHadith(UUID id) {
        if (!hadithRepository.existsById(id)) {
            throw new HadithNotFoundException();
        }
        hadithRepository.deleteById(id);
    }
}