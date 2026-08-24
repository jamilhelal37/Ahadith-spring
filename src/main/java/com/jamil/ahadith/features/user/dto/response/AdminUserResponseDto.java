package com.jamil.ahadith.features.user.dto.response;

import com.jamil.ahadith.features.user.entity.Gender;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminUserResponseDto {
    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;
    private UserStatus status;
    private Gender gender;
    private UserType type;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
