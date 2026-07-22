package com.jamil.ahadith.features.interaction.controller;

import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.interaction.dto.response.FavoriteResponseDto;
import com.jamil.ahadith.features.interaction.service.FavoriteService;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/me/favorites", "/api/v1/me/favorites"})
public class MeFavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping
    public SearchResponse<HadithSearchItemDto> getFavorites(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return favoriteService.getCurrentUserFavorites(page, size);
    }

    @PostMapping("/{hadithId}")
    public ResponseEntity<FavoriteResponseDto> addFavorite(@PathVariable UUID hadithId,
                                                           UriComponentsBuilder uriBuilder) {
        var favorite = favoriteService.createCurrentUserFavorite(hadithId);
        var uri = uriBuilder.path("/api/v1/me/favorites/{hadithId}").buildAndExpand(hadithId).toUri();
        return ResponseEntity.created(uri).body(favorite);
    }

    @DeleteMapping("/{hadithId}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable UUID hadithId) {
        favoriteService.deleteCurrentUserFavoriteByHadith(hadithId);
        return ResponseEntity.noContent().build();
    }
}
