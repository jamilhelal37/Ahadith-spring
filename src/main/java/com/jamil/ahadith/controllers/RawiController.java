package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.RawiRequestDto;
import com.jamil.ahadith.dtos.responses.RawiResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.updates.RawiUpdateDto;
import com.jamil.ahadith.services.AdminPageService;
import com.jamil.ahadith.services.RawiService;
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
@RequestMapping("/admin/rawis")
class RawiController {
        private final RawiService rawiService;
        private final AdminPageService adminPageService;

        @GetMapping
        public SearchResponse<RawiResponseDto> getRawis(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        @RequestParam(required = false) String sort) {
            return rawiService.getRawis(adminPageService.pageable(page, size, sort,
                    Set.of("name", "createdAt", "updatedAt", "id"),
                    Sort.by(Sort.Direction.ASC, "name").and(Sort.by("id"))));
        }

        @GetMapping("/{id}")
        public RawiResponseDto getRawiById(@PathVariable UUID id) {
            return rawiService.getRawiById(id);
        }

        @PostMapping
        public ResponseEntity<RawiResponseDto> createRawi(@Valid @RequestBody RawiRequestDto rawiRequest,
                                                          UriComponentsBuilder uriBuilder) {
            var rawi = rawiService.createRawi(rawiRequest);
            var uri = uriBuilder.path("/admin/rawis/{id}").buildAndExpand(rawi.getId()).toUri();
            return ResponseEntity.created(uri).body(rawi);
        }

        @PutMapping("/{id}")
        public RawiResponseDto updateRawi(@PathVariable UUID id,
                                          @Valid @RequestBody RawiUpdateDto rawiUpdate) {
            return rawiService.updateRawi(id, rawiUpdate);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteRawi(@PathVariable UUID id) {
            rawiService.deleteRawi(id);
            return ResponseEntity.noContent().build();
        }

    }
