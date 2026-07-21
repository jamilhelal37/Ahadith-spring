package com.jamil.ahadith.features.hadith.controller.admin;

import com.jamil.ahadith.features.hadith.entity.FakeHadith;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.hadith.dto.request.FakeHadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.FakeHadithResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.update.FakeHadithUpdateDto;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.hadith.service.FakeHadithService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/admin/fake-ahadith", "/api/v1/admin/fake-ahadith"})
public class FakeHadithController {
    private final FakeHadithService fakeHadithService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<FakeHadithResponseDto> getFakeAhadith(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size,
                                                                @RequestParam(required = false) String sort) {
        return fakeHadithService.getFakeAhadith(adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "updatedAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @GetMapping("/{id}")
    public FakeHadithResponseDto getFakeHadithById(@PathVariable UUID id) {
        return fakeHadithService.getFakeHadithById(id);
    }

    @PostMapping
    public ResponseEntity<FakeHadithResponseDto> createFakeHadith(@Valid @RequestBody FakeHadithRequestDto request,
                                                                 UriComponentsBuilder uriBuilder) {
        var fakeHadith = fakeHadithService.createFakeHadith(request);
        var uri = uriBuilder.path("/admin/fake-ahadith/{id}").buildAndExpand(fakeHadith.getId()).toUri();
        return ResponseEntity.created(uri).body(fakeHadith);
    }

    @PutMapping("/{id}")
    public FakeHadithResponseDto updateFakeHadith(@PathVariable UUID id,
                                                 @Valid @RequestBody FakeHadithUpdateDto request) {
        return fakeHadithService.updateFakeHadith(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFakeHadith(@PathVariable UUID id) {
        fakeHadithService.deleteFakeHadith(id);
        return ResponseEntity.noContent().build();
    }
}
