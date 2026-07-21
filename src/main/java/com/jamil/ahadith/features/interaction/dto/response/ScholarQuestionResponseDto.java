package com.jamil.ahadith.features.interaction.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
import com.jamil.ahadith.features.hadith.dto.response.publicapi.PublicHadithSummaryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScholarQuestionResponseDto {
    private UUID id;
    private PublicHadithSummaryResponseDto hadith;
    private AdminUserReferenceDto asker;
    private String askerText;
    private Boolean isActive;
    private String answerText;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
