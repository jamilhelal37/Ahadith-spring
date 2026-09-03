package com.jamil.ahadith.features.hadith.controller.publicapi;

import com.jamil.ahadith.features.search.service.HadithSearchService;
import com.jamil.ahadith.features.hadith.dto.response.publicapi.PublicHadithDetailsDto;
import com.jamil.ahadith.features.hadith.service.PublicHadithDetailsService;
import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/ahadith", "/api/v1/ahadith"})
public class PublicHadithController {
    private final HadithSearchService hadithSearchService;
    private final PublicHadithDetailsService publicHadithDetailsService;

    @PostMapping("/search")
    public SearchResponse<HadithSearchItemDto> search(@Valid @RequestBody(required = false) HadithSearchRequest request) {
        return hadithSearchService.publicSearch(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicHadithDetailsDto> details(@PathVariable UUID id) {
        PublicHadithDetailsDto response = publicHadithDetailsService.getDetails(id);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .body(response);
    }
}
