package com.jamil.ahadith.features.search.controller;

import com.jamil.ahadith.features.search.dto.response.SearchHistoryResponseDto;
import com.jamil.ahadith.features.search.service.SearchHistoryService;
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
    private final SearchHistoryService searchHistoryService;

    @GetMapping
    public List<SearchHistoryResponseDto> getRecentSearchHistory(@RequestParam(defaultValue = "10") int limit) {
        return searchHistoryService.getRecentSearchHistory(limit);
    }

    @GetMapping("/search")
    public List<SearchHistoryResponseDto> searchSearchHistory(@RequestParam(required = false) String keyword,
                                                              @RequestParam(defaultValue = "10") int limit) {
        return searchHistoryService.searchSearchHistory(keyword, limit);
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
