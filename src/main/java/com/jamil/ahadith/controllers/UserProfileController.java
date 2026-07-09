package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.responses.ProfileImageResponse;
import com.jamil.ahadith.exceptions.UserNotFoundException;
import com.jamil.ahadith.repositories.UserRepository;
import com.jamil.ahadith.services.UserProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/users/me")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final UserRepository userRepository;

    @PostMapping("/profile-image")
    public ProfileImageResponse uploadProfileImage(@RequestParam("file") MultipartFile file) {
        return userProfileService.uploadProfileImage(getCurrentUserId(), file);
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<Void> deleteProfileImage() {
        userProfileService.deleteProfileImage(getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .map(user -> user.getId())
                .orElseThrow(UserNotFoundException::new);
    }
}
