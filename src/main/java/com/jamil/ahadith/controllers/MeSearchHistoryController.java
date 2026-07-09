package com.jamil.ahadith.controllers;

import com.jamil.ahadith.entities.SearchHistory;
import com.jamil.ahadith.services.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/me/search-history")
public class MeSearchHistoryController {
    private final SearchService searchService;

    @GetMapping
    public List<SearchHistory> getRecentSearchHistory(@RequestParam(defaultValue = "10") int limit) {
        return searchService.getRecentSearchHistory(limit);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSearchHistory() {
        searchService.deleteCurrentUserSearchHistory();
        return ResponseEntity.noContent().build();
    }
}
