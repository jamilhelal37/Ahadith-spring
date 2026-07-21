package com.jamil.ahadith.features.interaction.service;

import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.core.web.dto.PaginationMeta;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.exception.ConflictException;
import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.interaction.entity.Favorite;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import com.jamil.ahadith.features.interaction.dto.response.FavoriteResponseDto;
import com.jamil.ahadith.features.interaction.exception.FavoriteNotFoundException;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.interaction.mapper.FavoriteMapper;
import com.jamil.ahadith.features.interaction.repository.FavoriteRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class FavoriteService {
    private static final int MAX_SIZE = 50;

    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final HadithRepository hadithRepository;
    private final AdminPageService adminPageService;
    private final HadithSearchService hadithSearchService;

    public SearchResponse<FavoriteResponseDto> getFavorites(Pageable pageable) {
        return adminPageService.response(favoriteRepository.findAll(pageable).map(favoriteMapper::toResponseDto));
    }

    @Transactional(readOnly = true)
    public SearchResponse<HadithSearchItemDto> getCurrentUserFavorites(int page, int size) {
        var user = currentUserService.requireCurrentUser();
        int safePage = validatePage(page);
        int safeSize = validateSize(size);
        Page<UUID> favoriteHadithIds = favoriteRepository.findFavoriteHadithIdsByUserId(
                user.getId(),
                PageRequest.of(safePage, safeSize));
        List<HadithSearchItemDto> items = hadithSearchService.getHadithCardsByIdsInOrder(favoriteHadithIds.getContent());

        return new SearchResponse<>(items, toPaginationMeta(favoriteHadithIds));
    }

    public FavoriteResponseDto createCurrentUserFavorite(UUID hadithId) {
        var user = currentUserService.requireCurrentUser();
        if (favoriteRepository.existsByUserIdAndHadithId(user.getId(), hadithId)) {
            throw new ConflictException("Favorite already exists");
        }
        var hadith = hadithRepository.findById(hadithId).orElseThrow(HadithNotFoundException::new);
        var favorite = new Favorite();
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

    private int validatePage(int page) {
        if (page < 0) {
            throw new InvalidRequestException("page must be greater than or equal to 0");
        }
        return page;
    }

    private int validateSize(int size) {
        if (size < 1) {
            throw new InvalidRequestException("size must be greater than 0");
        }
        return Math.min(size, MAX_SIZE);
    }

    private PaginationMeta toPaginationMeta(Page<?> page) {
        return new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
