package com.jamil.ahadith.features.hadith.dto.response;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExplainingResponseDto {
    private UUID id;
    private String text;
    private String normalText;
    private String searchText;
    private User createdBy;
    private User updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
