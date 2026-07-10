package com.jamil.ahadith.dtos.responses;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommentResponseDto {
    private UUID id;
    private SimpleReferenceDto hadith;
    private AdminUserReferenceDto user;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
