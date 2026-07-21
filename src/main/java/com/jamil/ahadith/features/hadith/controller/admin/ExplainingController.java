package com.jamil.ahadith.features.hadith.controller.admin;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.hadith.entity.Explaining;

import com.jamil.ahadith.features.hadith.dto.request.ExplainingRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.ExplainingResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.update.ExplainingUpdateDto;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.hadith.service.ExplainingService;
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
@RequestMapping({"/admin/explaining", "/api/v1/admin/explaining"})
public class ExplainingController {
    private final ExplainingService explainingService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<ExplainingResponseDto> getExplainings(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size,
                                                                @RequestParam(required = false) String sort) {
        return explainingService.getExplainings(adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "updatedAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @GetMapping("/{id}")
    public ExplainingResponseDto getExplainingById(@PathVariable UUID id) {
        return explainingService.getExplainingById(id);
    }

    @PostMapping
    public ResponseEntity<ExplainingResponseDto> createExplaining(@Valid @RequestBody ExplainingRequestDto request,
                                                                 UriComponentsBuilder uriBuilder) {
        var explaining = explainingService.createExplaining(request);
        var uri = uriBuilder.path("/admin/explaining/{id}").buildAndExpand(explaining.getId()).toUri();
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
