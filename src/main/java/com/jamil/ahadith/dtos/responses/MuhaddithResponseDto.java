package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.Gender;
import com.jamil.ahadith.entities.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MuhaddithResponseDto {
    private UUID id;
    private String name;
    private Gender gender;
    private String about;
    private User createdBy;
    private User updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
