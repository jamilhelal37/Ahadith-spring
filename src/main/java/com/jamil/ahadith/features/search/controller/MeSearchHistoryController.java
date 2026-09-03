package com.jamil.ahadith.features.search.controller;

import com.jamil.ahadith.features.search.dto.response.SearchHistoryResponseDto;
import com.jamil.ahadith.features.search.entity.SearchSource;
import com.jamil.ahadith.features.search.service.SearchHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/me/search-history", "/api/v1/me/search-history"})
public class MeSearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @GetMapping
    public List<SearchHistoryResponseDto> getRecentSearchHistory(
            @RequestParam SearchSource source,
            @RequestParam(defaultValue = "5") int limit) {

        return searchHistoryService.getRecentSearchHistory(source, limit);
    }

    @GetMapping("/search")
    public List<SearchHistoryResponseDto> searchSearchHistory(
            @RequestParam SearchSource source,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "5") int limit) {

        return searchHistoryService.searchSearchHistory(source, keyword, limit);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSearchHistory() {
        searchHistoryService.deleteCurrentUserSearchHistory();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSearchHistoryItem(@PathVariable UUID id) {
        searchHistoryService.deleteCurrentUserSearchHistoryItem(id);
        return ResponseEntity.noContent().build();
    }
}