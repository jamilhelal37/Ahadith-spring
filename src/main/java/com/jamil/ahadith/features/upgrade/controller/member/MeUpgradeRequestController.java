package com.jamil.ahadith.features.upgrade.controller.member;

import com.jamil.ahadith.features.upgrade.entity.UpgradeRequest;

import com.jamil.ahadith.features.upgrade.dto.request.UpgradeRequestDto;
import com.jamil.ahadith.features.upgrade.dto.response.UpgradeRequestResponseDto;
import com.jamil.ahadith.features.upgrade.service.UpgradeRequestService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/me/upgrade-requests")
public class MeUpgradeRequestController {
    private final UpgradeRequestService upgradeRequestService;

    @PostMapping
    public ResponseEntity<UpgradeRequestResponseDto> createUpgradeRequest(@Valid @RequestBody UpgradeRequestDto request,
                                                                          UriComponentsBuilder uriBuilder) {
        var upgradeRequest = upgradeRequestService.createUpgradeRequest(request);
        var uri = uriBuilder.path("/me/upgrade-requests/{id}").buildAndExpand(upgradeRequest.getId()).toUri();
        return ResponseEntity.created(uri).body(upgradeRequest);
    }

    @GetMapping("/current")
    public UpgradeRequestResponseDto getCurrentUpgradeRequest() {
        return upgradeRequestService.getCurrentUpgradeRequest();
    }

    @GetMapping
    public List<UpgradeRequestResponseDto> getMyUpgradeRequests() {
        return upgradeRequestService.getMyUpgradeRequests();
    }
}
