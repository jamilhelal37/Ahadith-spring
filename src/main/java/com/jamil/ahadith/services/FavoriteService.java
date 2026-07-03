package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.FavoriteRequestDto;
import com.jamil.ahadith.dtos.responses.FavoriteResponseDto;
import com.jamil.ahadith.exceptions.FavoriteNotFoundException;
import com.jamil.ahadith.mappers.FavoriteMapper;
import com.jamil.ahadith.repositories.FavoriteRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;
    private final EntityManager entityManager;

    public List<FavoriteResponseDto> getFavorites() {
        return favoriteRepository.findAll().stream()
                .map(favoriteMapper::toResponseDto)
                .toList();
    }

    public FavoriteResponseDto createFavorite(FavoriteRequestDto request) {
        var favorite = favoriteRepository.saveAndFlush(favoriteMapper.toEntity(request));
        entityManager.refresh(favorite);
        return favoriteMapper.toResponseDto(favorite);
    }

    public void deleteFavorite(UUID id) {
        if (!favoriteRepository.existsById(id)) {
            throw new FavoriteNotFoundException();
        }
        favoriteRepository.deleteById(id);
    }
}