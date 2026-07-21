package com.jamil.ahadith.features.interaction.controller;

import com.jamil.ahadith.features.interaction.dto.response.FavoriteResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.interaction.service.FavoriteService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/admin/favorites", "/api/v1/admin/favorites"})
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<FavoriteResponseDto> getFavorites(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size,
                                                            @RequestParam(required = false) String sort) {
        return favoriteService.getFavorites(adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable UUID id) {
        favoriteService.deleteFavorite(id);
        return ResponseEntity.noContent().build();
    }
}
