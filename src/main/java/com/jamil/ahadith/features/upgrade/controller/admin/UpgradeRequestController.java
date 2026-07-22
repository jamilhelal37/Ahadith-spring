package com.jamil.ahadith.features.upgrade.controller.admin;

import com.jamil.ahadith.features.upgrade.dto.request.UpgradeReviewRequestDto;
import com.jamil.ahadith.features.upgrade.dto.response.AdminUpgradeRequestResponseDto;
import com.jamil.ahadith.features.upgrade.dto.response.SignedDocumentDownloadResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.upgrade.entity.UpgradeStatus;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.upgrade.service.UpgradeRequestService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin/upgrade-requests")
public class UpgradeRequestController {
    private final UpgradeRequestService upgradeRequestService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<AdminUpgradeRequestResponseDto> getUpgradeRequests(
            @RequestParam(required = false) UpgradeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return upgradeRequestService.getUpgradeRequests(status, adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "updatedAt", "reviewedAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @GetMapping("/{id}")
    public AdminUpgradeRequestResponseDto getUpgradeRequestById(@PathVariable UUID id) {
        return upgradeRequestService.getUpgradeRequestById(id);
    }

    @GetMapping("/{id}/document")
    public ResponseEntity<SignedDocumentDownloadResponseDto> getDocument(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(upgradeRequestService.createAdminDownloadUrl(id));
    }

    @PatchMapping("/{id}/review")
    public AdminUpgradeRequestResponseDto review(@PathVariable UUID id,
                                                 @Valid @RequestBody UpgradeReviewRequestDto request) {
        return upgradeRequestService.review(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUpgradeRequest(@PathVariable UUID id) {
        upgradeRequestService.deleteUpgradeRequest(id);
        return ResponseEntity.noContent().build();
    }
}
