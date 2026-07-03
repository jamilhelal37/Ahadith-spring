package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.ExplainingRequestDto;
import com.jamil.ahadith.dtos.responses.ExplainingResponseDto;
import com.jamil.ahadith.dtos.updates.ExplainingUpdateDto;
import com.jamil.ahadith.services.ExplainingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/explaining")
public class ExplainingController {
    private final ExplainingService explainingService;

    @GetMapping
    public List<ExplainingResponseDto> getExplainings() {
        return explainingService.getExplainings();
    }

    @GetMapping("/{id}")
    public ExplainingResponseDto getExplainingById(@PathVariable UUID id) {
        return explainingService.getExplainingById(id);
    }

    @PostMapping
    public ResponseEntity<ExplainingResponseDto> createExplaining(@Valid @RequestBody ExplainingRequestDto request,
                                                                 UriComponentsBuilder uriBuilder) {
        var explaining = explainingService.createExplaining(request);
        var uri = uriBuilder.path("/explaining/{id}").buildAndExpand(explaining.getId()).toUri();
        return ResponseEntity.created(uri).body(explaining);
    }

    @PutMapping("/{id}")
    public ExplainingResponseDto updateExplaining(@PathVariable UUID id,
                                                 @Valid @RequestBody ExplainingUpdateDto request) {
        return explainingService.updateExplaining(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExplaining(@PathVariable UUID id) {
        explainingService.deleteExplaining(id);
        return ResponseEntity.noContent().build();
    }
}