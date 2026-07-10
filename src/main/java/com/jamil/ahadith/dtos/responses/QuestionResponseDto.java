package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.Hadith;
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
