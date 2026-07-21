package com.jamil.ahadith.features.search.controller;

import com.jamil.ahadith.features.search.dto.response.SearchHistoryResponseDto;
import com.jamil.ahadith.features.search.service.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/me/search-history", "/api/v1/me/search-history"})
public class MeSearchHistoryController {
    private final SearchService searchService;

    @GetMapping
    public List<SearchHistoryResponseDto> getRecentSearchHistory(@RequestParam(defaultValue = "10") int limit) {
        return searchService.getRecentSearchHistory(limit);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSearchHistory() {
        searchService.deleteCurrentUserSearchHistory();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSearchHistoryItem(@PathVariable UUID id) {
        searchService.deleteCurrentUserSearchHistoryItem(id);
        return ResponseEntity.noContent().build();
    }
}
