package com.jamil.ahadith.features.search.controller;

import com.jamil.ahadith.features.hadith.entity.HadithType;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.hadith.dto.response.HadithResponseDto;
import com.jamil.ahadith.features.search.dto.response.SearchHistoryResponseDto;
import com.jamil.ahadith.features.search.service.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/search", "/me/search"})
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public List<HadithResponseDto> searchHadiths(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String generalQuery,
            @RequestParam(required = false) UUID rawiId,
            @RequestParam(required = false) UUID rulingId,
            @RequestParam(required = false) String hadithType,
            @RequestParam(required = false) UUID topicId,
            @RequestParam(required = false) UUID muhaddithId,
            @RequestParam(required = false) UUID bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return searchService.searchHadiths(query, generalQuery, rawiId, rulingId, hadithType, topicId, muhaddithId, bookId, page, size);
    }

    @GetMapping("/history")
    public List<SearchHistoryResponseDto> getRecentSearchHistory(@RequestParam(defaultValue = "10") int limit) {
        return searchService.getRecentSearchHistory(limit);
    }

    @GetMapping("/history/search")
    public List<SearchHistoryResponseDto> searchSearchHistory(@RequestParam(required = false) String keyword,
                                                  @RequestParam(defaultValue = "10") int limit) {
        return searchService.searchSearchHistory(keyword, limit);
    }
}
