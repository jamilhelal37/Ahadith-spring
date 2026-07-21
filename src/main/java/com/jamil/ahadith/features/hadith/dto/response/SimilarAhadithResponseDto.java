package com.jamil.ahadith.features.hadith.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SimilarAhadithResponseDto {
    private UUID id;
    private HadithReferenceResponseDto mainHadith;
    private HadithReferenceResponseDto simHadith;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
