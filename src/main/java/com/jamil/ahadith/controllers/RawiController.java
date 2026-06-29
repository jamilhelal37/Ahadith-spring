package com.jamil.ahadith.controllers;
import java.util.List;

import java.util.UUID;
import com.jamil.ahadith.dtos.requests.RulingRequestDto;
import com.jamil.ahadith.dtos.responses.RulingResponseDto;
import com.jamil.ahadith.dtos.updates.RulingUpdateDto;
import com.jamil.ahadith.services.RulingService;
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
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@AllArgsConstructor
@RequestMapping("/rulings")
class RulingController {
        private final RulingService rulingService;

        @GetMapping
        public List<RulingResponseDto> getRulings() {
            return rulingService.getRulings();
        }

        @GetMapping("/{id}")
        public RulingResponseDto getRulingById(@PathVariable UUID id) {
            return rulingService.getRulingById(id);
        }

        @PostMapping
        public ResponseEntity<RulingResponseDto> createRuling(@Valid @RequestBody RulingRequestDto rulingRequest,
                                                          UriComponentsBuilder uriBuilder) {
            var ruling = rulingService.createRuling(rulingRequest);
            var uri = uriBuilder.path("/rulings/{id}").buildAndExpand(ruling.getId()).toUri();
            return ResponseEntity.created(uri).body(ruling);
        }

        @PutMapping("/{id}")
        public RulingResponseDto updateRuling(@PathVariable UUID id,
                                          @Valid @RequestBody RulingUpdateDto rulingUpdate) {
            return rulingService.updateRuling(id, rulingUpdate);
        }


    }
