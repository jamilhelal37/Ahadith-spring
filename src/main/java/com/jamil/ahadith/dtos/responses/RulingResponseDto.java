package com.jamil.ahadith.dtos.responses;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RulingResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
