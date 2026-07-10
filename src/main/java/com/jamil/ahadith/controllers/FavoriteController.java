package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.responses.FavoriteResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.services.AdminPageService;
import com.jamil.ahadith.services.FavoriteService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/favorites")
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
