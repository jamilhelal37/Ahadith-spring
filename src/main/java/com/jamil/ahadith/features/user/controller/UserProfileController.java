package com.jamil.ahadith.features.user.controller;

import com.jamil.ahadith.features.auth.dto.response.AuthUserDto;
import com.jamil.ahadith.core.web.dto.MessageResponseDto;
import com.jamil.ahadith.core.storage.dto.ProfileImageResponse;
import com.jamil.ahadith.features.user.dto.request.ChangePasswordRequestDto;
import com.jamil.ahadith.features.user.dto.request.UserProfileUpdateRequestDto;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.exception.UserNotFoundException;
import com.jamil.ahadith.features.auth.mapper.AuthUserMapper;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.features.auth.service.AuthService;
import com.jamil.ahadith.features.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@AllArgsConstructor
@RequestMapping({"/me", "/api/v1/me"})
@Tag(name = "Me")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuthUserMapper authUserMapper;

    @GetMapping
    @Operation(summary = "Get current user profile")
    public AuthUserDto getMe() {
        User user = getCurrentUser();
        return authUserMapper.toDto(user);
    }

    @PutMapping
    @Operation(summary = "Update current user profile")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                    {
                      "name": "User Name",
                      "gender": "male",
                      "birthDate": "2000-01-01"
                    }
                    """)
    ))
    public AuthUserDto updateMe(@Valid @RequestBody UserProfileUpdateRequestDto request) {
        return userProfileService.updateProfile(getCurrentUserId(), request);
    }

    @PutMapping("/password")
    @Operation(summary = "Change current user password")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                    {
                      "currentPassword": "old-password",
                      "newPassword": "new-password"
                    }
                    """)
    ))
    public MessageResponseDto changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        return userProfileService.changePassword(getCurrentUserId(), request);
    }

    @PostMapping("/profile-image")
    @Operation(summary = "Upload current user profile image")
    public ProfileImageResponse uploadProfileImage(@RequestParam("file") MultipartFile file) {
        return userProfileService.uploadProfileImage(getCurrentUserId(), file);
    }

    @DeleteMapping("/profile-image")
    @Operation(summary = "Delete current user profile image")
    public ResponseEntity<Void> deleteProfileImage() {
        userProfileService.deleteProfileImage(getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout all current user sessions")
    public MessageResponseDto logoutAll() {
        authService.logoutAll(getCurrentUser());
        return new MessageResponseDto("All sessions logged out");
    }

    private UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(UserNotFoundException::new);
    }

}
