package com.jamil.ahadith.features.notification.controller;

import com.jamil.ahadith.core.web.dto.MessageResponseDto;
import com.jamil.ahadith.features.notification.dto.request.FcmTokenRequestDto;
import com.jamil.ahadith.features.notification.service.UserFcmTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/me/fcm-tokens", "/api/v1/me/fcm-tokens"})
public class MeFcmTokenController {
    private final UserFcmTokenService userFcmTokenService;

    @PostMapping
    public ResponseEntity<MessageResponseDto> registerToken(
            @Valid @RequestBody FcmTokenRequestDto request
    ) {
        userFcmTokenService.registerCurrentUserToken(request.getFcmToken());
        return ResponseEntity.ok(new MessageResponseDto("FCM token registered"));
    }

    @DeleteMapping
    public ResponseEntity<MessageResponseDto> deleteToken(
            @Valid @RequestBody FcmTokenRequestDto request
    ) {
        userFcmTokenService.deleteCurrentUserToken(request.getFcmToken());
        return ResponseEntity.ok(new MessageResponseDto("FCM token removed"));
    }
}
