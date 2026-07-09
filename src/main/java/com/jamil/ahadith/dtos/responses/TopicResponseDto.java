package com.jamil.ahadith.dtos.responses;

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
