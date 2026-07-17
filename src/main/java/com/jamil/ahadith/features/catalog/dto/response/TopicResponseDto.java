package com.jamil.ahadith.features.catalog.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TopicResponseDto {
    private UUID id;
    private String name;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
