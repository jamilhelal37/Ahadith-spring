package com.jamil.ahadith.features.interaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentResponseDto {
    private UUID id;
    private String text;
    private HadithCommentReferenceDto hadith;
    private AdminCommentAuthorDto scholar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
