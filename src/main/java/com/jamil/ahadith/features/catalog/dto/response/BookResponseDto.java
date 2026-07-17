package com.jamil.ahadith.features.catalog.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookResponseDto {
    private UUID id;
    private String name;
    private Muhaddith muhaddith;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
