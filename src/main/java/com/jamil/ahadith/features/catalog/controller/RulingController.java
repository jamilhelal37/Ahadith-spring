package com.jamil.ahadith.features.catalog.controller;

import com.jamil.ahadith.features.catalog.dto.request.RulingRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.RulingResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.catalog.dto.update.RulingUpdateDto;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.catalog.service.RulingService;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/admin/rulings", "/api/v1/admin/rulings"})
class RulingController {
        private final RulingService rulingService;
        private final AdminPageService adminPageService;

        @GetMapping
        public SearchResponse<RulingResponseDto> getRulings(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size,
                                                            @RequestParam(required = false) String sort) {
            return rulingService.getRulings(adminPageService.pageable(page, size, sort,
                    Set.of("name", "createdAt", "updatedAt", "id"),
                    Sort.by(Sort.Direction.ASC, "name").and(Sort.by("id"))));
        }

        @GetMapping("/{id}")
        public RulingResponseDto getRulingById(@PathVariable UUID id) {
            return rulingService.getRulingById(id);
        }

        @PostMapping
        public ResponseEntity<RulingResponseDto> createRuling(@Valid @RequestBody RulingRequestDto rulingRequest,
                                                          UriComponentsBuilder uriBuilder) {
            var ruling = rulingService.createRuling(rulingRequest);
        var uri = uriBuilder.path("/api/v1/admin/rulings/{id}").buildAndExpand(ruling.getId()).toUri();
            return ResponseEntity.created(uri).body(ruling);
        }

        @PutMapping("/{id}")
        public RulingResponseDto updateRuling(@PathVariable UUID id,
                                          @Valid @RequestBody RulingUpdateDto rulingUpdate) {
            return rulingService.updateRuling(id, rulingUpdate);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteRuling(@PathVariable UUID id) {
            rulingService.deleteRuling(id);
            return ResponseEntity.noContent().build();
        }

    }
