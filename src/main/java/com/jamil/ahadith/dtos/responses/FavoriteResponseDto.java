package com.jamil.ahadith.dtos.responses;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FavoriteResponseDto {
    private UUID id;
    private AdminUserReferenceDto user;
    private SimpleReferenceDto hadith;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
