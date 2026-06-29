package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FavoriteResponseDto {
    private UUID id;
    private User user;
    private Hadith hadith;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
