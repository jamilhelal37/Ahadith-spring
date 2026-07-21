package com.jamil.ahadith.features.hadith.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FakeHadithResponseDto {
    private UUID id;
    private HadithReferenceResponseDto subValid;
    private String text;
    private String normalText;
    private String searchText;
    private RulingReferenceResponseDto ruling;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
