package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.responses.FavoriteResponseDto;
import com.jamil.ahadith.services.FavoriteService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/me/favorites")
public class MeFavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping
    public List<FavoriteResponseDto> getFavorites() {
        return favoriteService.getCurrentUserFavorites();
    }

    @PostMapping("/{hadithId}")
    public ResponseEntity<FavoriteResponseDto> addFavorite(@PathVariable UUID hadithId,
                                                           UriComponentsBuilder uriBuilder) {
        var favorite = favoriteService.createCurrentUserFavorite(hadithId);
        var uri = uriBuilder.path("/me/favorites/{hadithId}").buildAndExpand(hadithId).toUri();
        return ResponseEntity.created(uri).body(favorite);
    }

    @DeleteMapping("/{hadithId}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable UUID hadithId) {
        favoriteService.deleteCurrentUserFavoriteByHadith(hadithId);
        return ResponseEntity.noContent().build();
    }
}
