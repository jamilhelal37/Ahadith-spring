package com.jamil.ahadith.features.user.controller;

import com.jamil.ahadith.features.auth.dto.response.AuthUserDto;
import com.jamil.ahadith.core.web.dto.MessageResponseDto;
import com.jamil.ahadith.core.storage.dto.ProfileImageResponse;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.exception.UserNotFoundException;
import com.jamil.ahadith.features.auth.mapper.AuthUserMapper;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.features.auth.service.AuthService;
import com.jamil.ahadith.features.user.service.UserProfileService;
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
    private final AuthUserMapper authUserMapper;

    @GetMapping
    public AuthUserDto getMe() {
        User user = getCurrentUser();
        return authUserMapper.toDto(user);
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

}
