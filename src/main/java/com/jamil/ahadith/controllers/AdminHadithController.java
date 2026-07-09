package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.responses.AdminHadithSearchItemDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.services.HadithSearchService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping({"/api/admin/ahadith", "/admin/ahadith"})
public class AdminHadithController {
    private final HadithSearchService hadithSearchService;

    @GetMapping
    public SearchResponse<AdminHadithSearchItemDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return hadithSearchService.adminDashboardSearch(q, page, size);
    }
}
