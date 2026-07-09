package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.SimilarAhadithRequestDto;
import com.jamil.ahadith.dtos.responses.SimilarAhadithResponseDto;
import com.jamil.ahadith.dtos.updates.SimilarAhadithUpdateDto;
import com.jamil.ahadith.services.SimilarAhadithService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/similar-ahadith")
public class SimilarAhadithController {
    private final SimilarAhadithService similarAhadithService;

    @GetMapping
    public List<SimilarAhadithResponseDto> getSimilarAhadiths() {
        return similarAhadithService.getSimilarAhadiths();
    }

    @GetMapping("/{id}")
    public SimilarAhadithResponseDto getSimilarAhadithById(@PathVariable UUID id) {
        return similarAhadithService.getSimilarAhadithById(id);
    }

    @PostMapping
    public ResponseEntity<SimilarAhadithResponseDto> createSimilarAhadith(@Valid @RequestBody SimilarAhadithRequestDto request,
                                                                          UriComponentsBuilder uriBuilder) {
        var similar = similarAhadithService.createSimilarAhadith(request);
        var uri = uriBuilder.path("/admin/similar-ahadith/{id}").buildAndExpand(similar.getId()).toUri();
        return ResponseEntity.created(uri).body(similar);
    }

    @PutMapping("/{id}")
    public SimilarAhadithResponseDto updateSimilarAhadith(@PathVariable UUID id,
                                                           @Valid @RequestBody SimilarAhadithUpdateDto request) {
        return similarAhadithService.updateSimilarAhadith(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimilarAhadith(@PathVariable UUID id) {
        similarAhadithService.deleteSimilarAhadith(id);
        return ResponseEntity.noContent().build();
    }
}
