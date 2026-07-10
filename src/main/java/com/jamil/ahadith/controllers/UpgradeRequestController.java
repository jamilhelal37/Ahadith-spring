package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.UpgradeRequestDto;
import com.jamil.ahadith.dtos.requests.UpgradeReviewRequestDto;
import com.jamil.ahadith.dtos.responses.UpgradeRequestResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.entities.UpgradeStatus;
import com.jamil.ahadith.services.AdminPageService;
import com.jamil.ahadith.services.UpgradeRequestService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/upgrade-requests")
public class UpgradeRequestController {
    private final UpgradeRequestService upgradeRequestService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<UpgradeRequestResponseDto> getUpgradeRequests(@RequestParam(required = false) UpgradeStatus status,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size,
                                                                        @RequestParam(required = false) String sort) {
        return upgradeRequestService.getUpgradeRequests(status, adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "updatedAt", "reviewedAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @GetMapping("/{id}")
    public UpgradeRequestResponseDto getUpgradeRequestById(@PathVariable UUID id) {
        return upgradeRequestService.getUpgradeRequestById(id);
    }

    @PatchMapping("/{id}/review")
    public UpgradeRequestResponseDto review(@PathVariable UUID id,
                                            @Valid @RequestBody UpgradeReviewRequestDto request) {
        return upgradeRequestService.review(id, request);
    }

    @RequestMapping(value = {"/{id}", "/{id}/status"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
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
