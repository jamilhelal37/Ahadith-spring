package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.*;
import com.jamil.ahadith.dtos.responses.AuthResponseDto;
import com.jamil.ahadith.dtos.responses.MessageResponseDto;
import com.jamil.ahadith.services.ClientIpService;
import com.jamil.ahadith.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final ClientIpService clientIpService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request,
                                                 HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(request, servletRequest.getHeader("User-Agent"),
                clientIpService.resolve(servletRequest)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request,
                                                   HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken(), servletRequest.getHeader("User-Agent"),
                clientIpService.resolve(servletRequest)));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logout(@RequestBody(required = false) LogoutRequestDto request) {
        authService.logout(request == null ? null : request.getRefreshToken());
        return ResponseEntity.ok(new MessageResponseDto("Logged out"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponseDto> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto request) {
        return ResponseEntity.ok(authService.verifyEmail(request.getToken()));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponseDto> resendVerification(@Valid @RequestBody ResendVerificationRequestDto request) {
        return ResponseEntity.ok(authService.resendVerification(request.getEmail()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDto> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDto> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
