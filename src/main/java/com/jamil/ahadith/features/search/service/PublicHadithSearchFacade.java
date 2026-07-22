package com.jamil.ahadith.features.search.service;

import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicHadithSearchFacade {
    private final HadithSearchService hadithSearchService;
    private final SearchHistoryService searchHistoryService;

    @Transactional
    public SearchResponse<HadithSearchItemDto> search(HadithSearchRequest request) {
        SearchResponse<HadithSearchItemDto> response = hadithSearchService.publicSearch(request);
        searchHistoryService.saveCurrentUserHadithSearch(request);
        return response;
    }
}
