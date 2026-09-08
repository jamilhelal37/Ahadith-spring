package com.jamil.ahadith.features.upgrade.controller.member;

import com.jamil.ahadith.features.upgrade.dto.request.UpgradeRequestCreateDto;
import com.jamil.ahadith.features.upgrade.dto.response.MemberUpgradeRequestResponseDto;
import com.jamil.ahadith.features.upgrade.dto.response.SignedDocumentDownloadResponseDto;
import com.jamil.ahadith.features.upgrade.service.UpgradeRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/me/upgrade-requests")
public class MeUpgradeRequestController {
    private final UpgradeRequestService upgradeRequestService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberUpgradeRequestResponseDto> createUpgradeRequest(
            @Valid @ModelAttribute UpgradeRequestCreateDto request,
            HttpServletRequest servletRequest,
            UriComponentsBuilder uriBuilder
    ) {
        var upgradeRequest = upgradeRequestService.createUpgradeRequest(request, servletRequest);
        var uri = uriBuilder.path("/api/v1/me/upgrade-requests/{id}").buildAndExpand(upgradeRequest.getId()).toUri();
        return ResponseEntity.created(uri).body(upgradeRequest);
    }

    @GetMapping("/current")
    public MemberUpgradeRequestResponseDto getCurrentUpgradeRequest() {
        return upgradeRequestService.getCurrentUpgradeRequest();
    }

    @GetMapping
    public MemberUpgradeRequestResponseDto getMyUpgradeRequest() {
        return upgradeRequestService.getMyUpgradeRequest();
    }
    @GetMapping("/{id}/document")
    public ResponseEntity<SignedDocumentDownloadResponseDto> getDocument(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(upgradeRequestService.createMemberDownloadUrl(id));
    }
}
