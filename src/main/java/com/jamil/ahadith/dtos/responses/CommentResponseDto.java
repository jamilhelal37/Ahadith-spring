package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommentResponseDto {
    private UUID id;
    private Hadith hadith;
    private User user;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
