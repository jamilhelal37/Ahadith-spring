package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.responses.AuthUserDto;
import com.jamil.ahadith.dtos.responses.MessageResponseDto;
import com.jamil.ahadith.dtos.responses.ProfileImageResponse;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.exceptions.UserNotFoundException;
import com.jamil.ahadith.repositories.UserRepository;
import com.jamil.ahadith.services.AuthService;
import com.jamil.ahadith.services.UserProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@AllArgsConstructor
@RequestMapping("/me")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    private final AuthService authService;

    @GetMapping
    public AuthUserDto getMe() {
        User user = getCurrentUser();
        return toAuthUserDto(user);
    }

    @PostMapping("/profile-image")
    public ProfileImageResponse uploadProfileImage(@RequestParam("file") MultipartFile file) {
        return userProfileService.uploadProfileImage(getCurrentUserId(), file);
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<Void> deleteProfileImage() {
        userProfileService.deleteProfileImage(getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
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

    private AuthUserDto toAuthUserDto(User user) {
        return AuthUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus() == null ? null : user.getStatus().name())
                .gender(user.getGender() == null ? null : user.getGender().name())
                .type(user.getType() == null ? null : user.getType().name())
                .birthDate(user.getBirthDate())
                .build();
    }
}
