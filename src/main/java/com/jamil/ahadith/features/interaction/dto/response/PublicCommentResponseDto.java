package com.jamil.ahadith.features.interaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicCommentResponseDto {
    private UUID id;
    private String text;
    private PublicScholarReferenceDto scholar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
