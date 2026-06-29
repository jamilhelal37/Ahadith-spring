package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.User;
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
