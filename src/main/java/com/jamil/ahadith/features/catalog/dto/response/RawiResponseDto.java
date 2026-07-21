package com.jamil.ahadith.features.catalog.dto.response;


import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;

import com.jamil.ahadith.features.user.entity.Gender;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RawiResponseDto {
    private UUID id;
    private String name;
    private Gender gender;
    private String about;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
