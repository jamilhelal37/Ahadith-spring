package com.jamil.ahadith.features.hadith.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RawiReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.ExplainingReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HadithResponseDto {
    private UUID id;
    private HadithReferenceResponseDto subValid;
    private ExplainingReferenceResponseDto explaining;
    private String type;
    private String text;
    private String normalText;
    private String searchText;
    private Integer hadithNumber;
    private RulingReferenceResponseDto ruling;
    private RawiReferenceResponseDto rawi;
    private BookReferenceResponseDto book;
    private String sanad;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
