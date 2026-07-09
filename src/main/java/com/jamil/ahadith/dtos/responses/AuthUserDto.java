package com.jamil.ahadith.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserDto {
    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;
    private String status;
    private String gender;
    private String type;
    private LocalDate birthDate;
}
