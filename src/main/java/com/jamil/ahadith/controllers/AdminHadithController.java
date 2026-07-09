package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.HadithRequestDto;
import com.jamil.ahadith.dtos.responses.AdminHadithSearchItemDto;
import com.jamil.ahadith.dtos.responses.HadithResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.updates.HadithUpdateDto;
import com.jamil.ahadith.services.HadithService;
import com.jamil.ahadith.services.HadithSearchService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/ahadith")
public class AdminHadithController {
    private final HadithSearchService hadithSearchService;
    private final HadithService hadithService;

    @GetMapping
    public SearchResponse<AdminHadithSearchItemDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return hadithSearchService.adminDashboardSearch(q, page, size);
    }

    @GetMapping("/{id}")
    public HadithResponseDto getById(@PathVariable UUID id) {
        return hadithService.getHadithById(id);
    }

    @PostMapping
    public ResponseEntity<HadithResponseDto> create(@Valid @RequestBody HadithRequestDto request,
                                                    UriComponentsBuilder uriBuilder) {
        var hadith = hadithService.createHadith(request);
        var uri = uriBuilder.path("/admin/ahadith/{id}").buildAndExpand(hadith.getId()).toUri();
        return ResponseEntity.created(uri).body(hadith);
    }

    @PutMapping("/{id}")
    public HadithResponseDto update(@PathVariable UUID id, @Valid @RequestBody HadithUpdateDto request) {
        return hadithService.updateHadith(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        hadithService.deleteHadith(id);
        return ResponseEntity.noContent().build();
    }
}
