package com.jamil.ahadith.features.hadith.controller.admin;

import com.jamil.ahadith.features.hadith.dto.request.HadithRequestDto;
import com.jamil.ahadith.features.search.dto.response.AdminHadithSearchItemDto;
import com.jamil.ahadith.features.hadith.dto.response.HadithResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.update.HadithUpdateDto;
import com.jamil.ahadith.features.hadith.dto.update.HadithPatchDto;
import com.jamil.ahadith.features.hadith.service.HadithService;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/admin/ahadith", "/api/v1/admin/ahadith"})
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
        var uri = uriBuilder.path("/api/v1/admin/ahadith/{id}").buildAndExpand(hadith.getId()).toUri();
        return ResponseEntity.created(uri).body(hadith);
    }

    @PutMapping("/{id}")
    public HadithResponseDto update(@PathVariable UUID id, @Valid @RequestBody HadithUpdateDto request) {
        return hadithService.updateHadith(id, request);
    }

    @PatchMapping("/{id}")
    public HadithResponseDto patch(@PathVariable UUID id, @Valid @RequestBody HadithPatchDto request) {
        return hadithService.patchHadith(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        hadithService.deleteHadith(id);
        return ResponseEntity.noContent().build();
    }
}
