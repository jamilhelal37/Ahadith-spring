package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.FavoriteRequestDto;
import com.jamil.ahadith.dtos.responses.FavoriteResponseDto;
import com.jamil.ahadith.services.FavoriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping
    public List<FavoriteResponseDto> getFavorites() {
        return favoriteService.getFavorites();
    }

    @PostMapping
    public ResponseEntity<FavoriteResponseDto> createFavorite(@Valid @RequestBody FavoriteRequestDto request,
                                                             UriComponentsBuilder uriBuilder) {
        var favorite = favoriteService.createFavorite(request);
        var uri = uriBuilder.path("/favorites/{id}").buildAndExpand(favorite.getId()).toUri();
        return ResponseEntity.created(uri).body(favorite);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable UUID id) {
        favoriteService.deleteFavorite(id);
        return ResponseEntity.noContent().build();
    }
}
