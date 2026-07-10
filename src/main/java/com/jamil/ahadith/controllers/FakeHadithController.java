package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.FakeHadithRequestDto;
import com.jamil.ahadith.dtos.responses.FakeHadithResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.updates.FakeHadithUpdateDto;
import com.jamil.ahadith.services.AdminPageService;
import com.jamil.ahadith.services.FakeHadithService;
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
@RequestMapping("/admin/fake-ahadith")
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
