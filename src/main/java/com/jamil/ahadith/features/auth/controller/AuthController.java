package com.jamil.ahadith.features.auth.controller;

import com.jamil.ahadith.features.account.dto.request.ForgotPasswordRequestDto;
import com.jamil.ahadith.features.auth.dto.request.GoogleLoginRequestDto;
import com.jamil.ahadith.features.auth.dto.request.LoginRequestDto;
import com.jamil.ahadith.features.auth.dto.request.LogoutRequestDto;
import com.jamil.ahadith.features.auth.dto.request.RefreshTokenRequestDto;
import com.jamil.ahadith.features.auth.dto.request.RegisterRequestDto;
import com.jamil.ahadith.features.account.dto.request.ResendVerificationRequestDto;
import com.jamil.ahadith.features.account.dto.request.ResetPasswordRequestDto;
import com.jamil.ahadith.features.account.dto.request.VerifyEmailRequestDto;
import com.jamil.ahadith.features.auth.dto.response.AuthResponseDto;
import com.jamil.ahadith.core.web.dto.MessageResponseDto;
import com.jamil.ahadith.features.account.service.EmailVerificationService;
import com.jamil.ahadith.features.account.service.PasswordResetService;
import com.jamil.ahadith.features.auth.service.AuthService;
import com.jamil.ahadith.core.web.ClientIpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
@RequiredArgsConstructor
public class AuthController {

    private static final String LOGOUT_SUCCESS_MESSAGE = "Logged out";

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final ClientIpService clientIpService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        AuthResponseDto response = authService.register(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest servletRequest
    ) {
        ClientMetadata clientMetadata =
                resolveClientMetadata(servletRequest);

        AuthResponseDto response = authService.login(
                request,
                clientMetadata.userAgent(),
                clientMetadata.ipAddress()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDto> googleLogin(
            @Valid @RequestBody GoogleLoginRequestDto request,
            HttpServletRequest servletRequest
    ) {
        ClientMetadata clientMetadata =
                resolveClientMetadata(servletRequest);

        AuthResponseDto response = authService.loginWithGoogle(
                request,
                clientMetadata.userAgent(),
                clientMetadata.ipAddress()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request,
            HttpServletRequest servletRequest
    ) {
        ClientMetadata clientMetadata =
                resolveClientMetadata(servletRequest);

        AuthResponseDto response = authService.refreshToken(
                request.getRefreshToken(),
                clientMetadata.userAgent(),
                clientMetadata.ipAddress()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logout(
            @Valid @RequestBody LogoutRequestDto request
    ) {
        String refreshToken =
                request == null ? null : request.getRefreshToken();

        authService.logout(refreshToken);

        return ResponseEntity.ok(
                new MessageResponseDto(LOGOUT_SUCCESS_MESSAGE)
        );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponseDto> verifyEmail(
            @Valid @RequestBody VerifyEmailRequestDto request
    ) {
        MessageResponseDto response =
                emailVerificationService.verifyEmail(request.getToken());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponseDto> resendVerification(
            @Valid @RequestBody ResendVerificationRequestDto request
    ) {
        MessageResponseDto response =
                emailVerificationService.resendVerification(request.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request
    ) {
        MessageResponseDto response =
                passwordResetService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request
    ) {
        MessageResponseDto response =
                passwordResetService.resetPassword(request);

        return ResponseEntity.ok(response);
    }

    private ClientMetadata resolveClientMetadata(
            HttpServletRequest request
    ) {
        String userAgent =
                request.getHeader(HttpHeaders.USER_AGENT);

        String ipAddress =
                clientIpService.resolve(request);

        return new ClientMetadata(userAgent, ipAddress);
    }

    private record ClientMetadata(
            String userAgent,
            String ipAddress
    ) {
    }
}
