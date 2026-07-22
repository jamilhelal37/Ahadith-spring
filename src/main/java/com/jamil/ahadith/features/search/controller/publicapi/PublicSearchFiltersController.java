package com.jamil.ahadith.features.search.controller.publicapi;

import com.jamil.ahadith.features.search.dto.response.FiltersListResponseDto;
import com.jamil.ahadith.features.search.service.SearchFiltersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicSearchFiltersController {
    private final SearchFiltersService searchFiltersService;

    @GetMapping({"/filterslist", "/ahadith/search/filters", "/api/v1/search/filters"})
    public FiltersListResponseDto getFiltersList() {
        return searchFiltersService.getFiltersList();
    }
}
