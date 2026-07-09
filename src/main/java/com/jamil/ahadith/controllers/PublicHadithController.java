package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.HadithSearchRequest;
import com.jamil.ahadith.dtos.responses.HadithDetailsDto;
import com.jamil.ahadith.dtos.responses.HadithFiltersDto;
import com.jamil.ahadith.dtos.responses.HadithSearchItemDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.services.HadithSearchService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/ahadith")
public class PublicHadithController {
    private final HadithSearchService hadithSearchService;

    @PostMapping("/search")
    public SearchResponse<HadithSearchItemDto> search(@RequestBody(required = false) HadithSearchRequest request) {
        return hadithSearchService.publicSearch(request);
    }

    @GetMapping("/search/filters")
    public HadithFiltersDto filters() {
        return hadithSearchService.getFilters();
    }

    @GetMapping("/{id}")
    public HadithDetailsDto details(@PathVariable UUID id) {
        return hadithSearchService.getDetails(id);
    }
}
