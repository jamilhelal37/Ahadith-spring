package com.jamil.ahadith.features.interaction.dto.response;

import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;
import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
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
