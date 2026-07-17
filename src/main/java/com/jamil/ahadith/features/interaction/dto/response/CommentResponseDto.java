package com.jamil.ahadith.features.interaction.dto.response;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;

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
