package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.LoginRequestDto;
import com.jamil.ahadith.dtos.requests.RegisterRequestDto;
import com.jamil.ahadith.dtos.responses.AuthResponseDto;
import com.jamil.ahadith.dtos.responses.AuthUserDto;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.entities.UserStatus;
import com.jamil.ahadith.entities.UserType;
import com.jamil.ahadith.exceptions.UserAlreadyExistsException;
import com.jamil.ahadith.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setType(UserType.member); // Default type
        user.setStatus(UserStatus.active); // Assuming active is a status

        userRepository.save(user);

        return createAuthResponse(user);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return createAuthResponse(user);
    }

    public AuthResponseDto refreshToken(String refreshToken) {
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String email = jwtService.getSubject(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return createAuthResponse(user);
    }

    private AuthResponseDto createAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .user(toAuthUserDto(user))
                .build();
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
