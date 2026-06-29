package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SimilarAhadithResponseDto {
    private UUID id;
    private Hadith mainHadith;
    private Hadith simHadith;
    private User createdBy;
    private User updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
