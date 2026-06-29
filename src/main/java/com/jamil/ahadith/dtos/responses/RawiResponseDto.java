package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.Gender;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RawiResponseDto {
    private UUID id;
    private String name;
    private Gender gender;
    private String about;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
