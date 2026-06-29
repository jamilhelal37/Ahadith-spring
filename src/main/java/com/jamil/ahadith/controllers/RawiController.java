package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.RawiRequestDto;
import com.jamil.ahadith.dtos.requests.RulingRequestDto;
import com.jamil.ahadith.dtos.responses.RawiResponseDto;
import com.jamil.ahadith.dtos.responses.RulingResponseDto;
import com.jamil.ahadith.dtos.updates.RawiUpdateDto;
import com.jamil.ahadith.dtos.updates.RulingUpdateDto;
import com.jamil.ahadith.services.RawiService;
import com.jamil.ahadith.services.RulingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/rawies")
class RawiController {
        private final RawiService rawiService;

        @GetMapping
        public List<RawiResponseDto> getRawies() {
            return rawiService.getRawies();
        }

        @GetMapping("/{id}")
        public RawiResponseDto getRawiById(@PathVariable UUID id) {
            return rawiService.getRawiById(id);
        }

        @PostMapping
        public ResponseEntity<RawiResponseDto> createRawi(@Valid @RequestBody RawiRequestDto rawiRequest,
                                                          UriComponentsBuilder uriBuilder) {
            var rawi = rawiService.createRawi(rawiRequest);
            var uri = uriBuilder.path("/rawies/{id}").buildAndExpand(rawi.getId()).toUri();
            return ResponseEntity.created(uri).body(rawi);
        }

        @PutMapping("/{id}")
        public RawiResponseDto updateRawi(@PathVariable UUID id,
                                          @Valid @RequestBody RawiUpdateDto rawiUpdate) {
            return rawiService.updateRawi(id, rawiUpdate);
        }

    }
