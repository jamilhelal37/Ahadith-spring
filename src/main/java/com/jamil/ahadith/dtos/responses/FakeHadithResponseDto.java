package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.Ruling;
import com.jamil.ahadith.entities.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FakeHadithResponseDto {
    private UUID id;
    private Hadith subValid;
    private String text;
    private String normalText;
    private String searchText;
    private Ruling ruling;
    private User createdBy;
    private User updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
