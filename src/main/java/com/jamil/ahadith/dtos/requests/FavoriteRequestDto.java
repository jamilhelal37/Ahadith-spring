package com.jamil.ahadith.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FavoriteRequestDto {
    @NotNull(message = "Hadith is required")
    private UUID hadithId;
}
