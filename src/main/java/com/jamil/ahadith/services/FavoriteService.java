package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.FavoriteRequestDto;
import com.jamil.ahadith.dtos.responses.FavoriteResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.exceptions.ConflictException;
import com.jamil.ahadith.exceptions.FavoriteNotFoundException;
import com.jamil.ahadith.exceptions.HadithNotFoundException;
import com.jamil.ahadith.mappers.FavoriteMapper;
import com.jamil.ahadith.repositories.FavoriteRepository;
import com.jamil.ahadith.repositories.HadithRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final HadithRepository hadithRepository;
    private final AdminPageService adminPageService;

    public SearchResponse<FavoriteResponseDto> getFavorites(Pageable pageable) {
        return adminPageService.response(favoriteRepository.findAll(pageable).map(favoriteMapper::toResponseDto));
    }

    public List<FavoriteResponseDto> getCurrentUserFavorites() {
        var user = currentUserService.requireCurrentUser();
        return favoriteRepository.findByUserId(user.getId()).stream()
                .map(favoriteMapper::toResponseDto)
                .toList();
    }

    public FavoriteResponseDto createFavorite(FavoriteRequestDto request) {
        return createCurrentUserFavorite(request.getHadithId());
    }

    public FavoriteResponseDto createCurrentUserFavorite(UUID hadithId) {
        var user = currentUserService.requireCurrentUser();
        if (favoriteRepository.existsByUserIdAndHadithId(user.getId(), hadithId)) {
            throw new ConflictException("Favorite already exists");
        }
        var hadith = hadithRepository.findById(hadithId).orElseThrow(HadithNotFoundException::new);
        var favorite = new com.jamil.ahadith.entities.Favorite();
        favorite.setUser(user);
        favorite.setHadith(hadith);
        try {
            favorite = favoriteRepository.saveAndFlush(favorite);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Favorite already exists");
        }
        entityManager.refresh(favorite);
        return favoriteMapper.toResponseDto(favorite);
    }

    public void deleteFavorite(UUID id) {
        if (!favoriteRepository.existsById(id)) {
            throw new FavoriteNotFoundException();
        }
        favoriteRepository.deleteById(id);
    }

    public void deleteCurrentUserFavoriteByHadith(UUID hadithId) {
        var user = currentUserService.requireCurrentUser();
        var favorite = favoriteRepository.findByUserIdAndHadithId(user.getId(), hadithId)
                .orElseThrow(FavoriteNotFoundException::new);
        favoriteRepository.delete(favorite);
    }
}
