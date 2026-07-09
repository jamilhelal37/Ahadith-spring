package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.HadithRequestDto;
import com.jamil.ahadith.dtos.responses.HadithDto;
import com.jamil.ahadith.dtos.responses.HadithResponseDto;
import com.jamil.ahadith.dtos.updates.HadithUpdateDto;
import com.jamil.ahadith.services.HadithService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/ahadith")
public class HadithController {
    private final HadithService hadithService;

    @GetMapping
    public List<HadithDto> getAhadith() {
        return hadithService.getAhadith();
    }

    @GetMapping("/{id}")
    public HadithResponseDto getHadithById(@PathVariable UUID id) {
        return hadithService.getHadithById(id);
    }

    @PostMapping
    public ResponseEntity<HadithResponseDto> createHadith(@Valid @RequestBody HadithRequestDto request,
                                                         UriComponentsBuilder uriBuilder) {
        var hadith = hadithService.createHadith(request);
        var uri = uriBuilder.path("/ahadith/{id}").buildAndExpand(hadith.getId()).toUri();
        return ResponseEntity.created(uri).body(hadith);
    }

    @PutMapping("/{id}")
    public HadithResponseDto updateHadith(@PathVariable UUID id,
                                         @Valid @RequestBody HadithUpdateDto request) {
        return hadithService.updateHadith(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHadith(@PathVariable UUID id) {
        hadithService.deleteHadith(id);
        return ResponseEntity.noContent().build();
    }
}
