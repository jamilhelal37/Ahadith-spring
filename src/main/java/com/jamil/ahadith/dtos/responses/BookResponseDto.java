package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.Muhaddith;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookResponseDto {
    private UUID id;
    private String name;
    private Muhaddith muhaddith;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
