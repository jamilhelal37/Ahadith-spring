package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.MuhaddithRequestDto;
import com.jamil.ahadith.dtos.responses.MuhaddithResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.updates.MuhaddithUpdateDto;
import com.jamil.ahadith.services.AdminPageService;
import com.jamil.ahadith.services.MuhaddithService;
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
@RequestMapping("/admin/muhaddiths")
public class MuhaddithController {
    private final MuhaddithService muhaddithService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<MuhaddithResponseDto> getMuhaddiths(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size,
                                                              @RequestParam(required = false) String sort) {
        return muhaddithService.getMuhaddiths(adminPageService.pageable(page, size, sort,
                Set.of("name", "createdAt", "updatedAt", "id"),
                Sort.by(Sort.Direction.ASC, "name").and(Sort.by("id"))));
    }

    @GetMapping("/{id}")
    public MuhaddithResponseDto getMuhaddithById(@PathVariable UUID id) {
        return muhaddithService.getMuhaddithById(id);
    }

    @PostMapping
    public ResponseEntity<MuhaddithResponseDto> createMuhaddith(@Valid @RequestBody MuhaddithRequestDto request,
                                                               UriComponentsBuilder uriBuilder) {
        var muhaddith = muhaddithService.createMuhaddith(request);
        var uri = uriBuilder.path("/admin/muhaddiths/{id}").buildAndExpand(muhaddith.getId()).toUri();
        return ResponseEntity.created(uri).body(muhaddith);
    }

    @PutMapping("/{id}")
    public MuhaddithResponseDto updateMuhaddith(@PathVariable UUID id,
                                               @Valid @RequestBody MuhaddithUpdateDto request) {
        return muhaddithService.updateMuhaddith(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMuhaddith(@PathVariable UUID id) {
        muhaddithService.deleteMuhaddith(id);
        return ResponseEntity.noContent().build();
    }
}
