package com.jamil.ahadith.features.hadith.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExplainingResponseDto {
    private UUID id;
    private String text;
    private String normalText;
    private String searchText;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
