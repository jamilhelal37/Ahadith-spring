package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.FakeHadithRequestDto;
import com.jamil.ahadith.dtos.responses.FakeHadithResponseDto;
import com.jamil.ahadith.dtos.updates.FakeHadithUpdateDto;
import com.jamil.ahadith.services.FakeHadithService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/fake-ahadith")
public class FakeHadithController {
    private final FakeHadithService fakeHadithService;

    @GetMapping
    public List<FakeHadithResponseDto> getFakeAhadith() {
        return fakeHadithService.getFakeAhadith();
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
