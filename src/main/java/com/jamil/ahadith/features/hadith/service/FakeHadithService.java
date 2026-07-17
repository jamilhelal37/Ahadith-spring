package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.hadith.entity.FakeHadith;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.hadith.dto.request.FakeHadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.FakeHadithResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.update.FakeHadithUpdateDto;
import com.jamil.ahadith.features.hadith.exception.FakeHadithNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.FakeHadithMapper;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
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
public class FakeHadithService {
    private final FakeHadithRepository fakeHadithRepository;
    private final FakeHadithMapper fakeHadithMapper;
    private final EntityManager entityManager;
    private final AdminPageService adminPageService;

    public SearchResponse<FakeHadithResponseDto> getFakeAhadith(Pageable pageable) {
        return adminPageService.response(fakeHadithRepository.findAll(pageable).map(fakeHadithMapper::toResponseDto));
    }

    public FakeHadithResponseDto getFakeHadithById(UUID id) {
        return fakeHadithRepository.findById(id)
                .map(fakeHadithMapper::toResponseDto)
                .orElseThrow(FakeHadithNotFoundException::new);
    }

    public FakeHadithResponseDto createFakeHadith(FakeHadithRequestDto request) {
        var fakeHadith = fakeHadithRepository.saveAndFlush(fakeHadithMapper.toEntity(request));
        entityManager.refresh(fakeHadith);
        return fakeHadithMapper.toResponseDto(fakeHadith);
    }

    public FakeHadithResponseDto updateFakeHadith(UUID id, FakeHadithUpdateDto request) {
        var fakeHadith = fakeHadithRepository.findById(id).orElseThrow(FakeHadithNotFoundException::new);
        fakeHadithMapper.updateEntity(request, fakeHadith);
        var savedFakeHadith = fakeHadithRepository.saveAndFlush(fakeHadith);
        entityManager.refresh(savedFakeHadith);
        return fakeHadithMapper.toResponseDto(savedFakeHadith);
    }

    public void deleteFakeHadith(UUID id) {
        if (!fakeHadithRepository.existsById(id)) {
            throw new FakeHadithNotFoundException();
        }
        fakeHadithRepository.deleteById(id);
    }
}
