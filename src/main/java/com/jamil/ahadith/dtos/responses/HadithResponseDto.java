package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HadithResponseDto {
    private UUID id;
    private Hadith subValid;
    private Explaining explaining;
    private String type;
    private String text;
    private String normalText;
    private String searchText;
    private Integer hadithNumber;
    private Ruling ruling;
    private Rawi rawi;
    private Book book;
    private String sanad;
    private User createdBy;
    private User updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
