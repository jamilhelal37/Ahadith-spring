package com.jamil.ahadith.features.interaction.dto.response;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;

import com.jamil.ahadith.features.hadith.entity.Hadith;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class QuestionResponseDto {
    private UUID id;
    private Hadith hadith;
    private AdminUserReferenceDto asker;
    private String askerText;
    private Boolean isActive;
    private String answerText;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
