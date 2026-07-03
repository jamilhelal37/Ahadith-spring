package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.UpgradeRequestDto;
import com.jamil.ahadith.dtos.responses.UpgradeRequestResponseDto;
import com.jamil.ahadith.services.UpgradeRequestService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/upgrade-requests")
public class UpgradeRequestController {
    private final UpgradeRequestService upgradeRequestService;

    @GetMapping
    public List<UpgradeRequestResponseDto> getUpgradeRequests() {
        return upgradeRequestService.getUpgradeRequests();
    }

    @GetMapping("/{id}")
    public UpgradeRequestResponseDto getUpgradeRequestById(@PathVariable UUID id) {
        return upgradeRequestService.getUpgradeRequestById(id);
    }

    @PostMapping
    public ResponseEntity<UpgradeRequestResponseDto> createUpgradeRequest(@Valid @RequestBody UpgradeRequestDto request,
                                                                          UriComponentsBuilder uriBuilder) {
        var upgradeRequest = upgradeRequestService.createUpgradeRequest(request);
        var uri = uriBuilder.path("/upgrade-requests/{id}").buildAndExpand(upgradeRequest.getId()).toUri();
        return ResponseEntity.created(uri).body(upgradeRequest);
    }

    @PutMapping("/{id}")
    public UpgradeRequestResponseDto updateUpgradeRequest(@PathVariable UUID id,
                                                          @Valid @RequestBody UpgradeRequestDto request) {
        return upgradeRequestService.updateUpgradeRequest(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUpgradeRequest(@PathVariable UUID id) {
        upgradeRequestService.deleteUpgradeRequest(id);
        return ResponseEntity.noContent().build();
    }
}
