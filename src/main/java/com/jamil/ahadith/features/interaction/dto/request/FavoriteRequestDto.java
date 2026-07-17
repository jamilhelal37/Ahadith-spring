package com.jamil.ahadith.features.interaction.dto.request;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FavoriteRequestDto {
    @NotNull(message = "Hadith is required")
    private UUID hadithId;
}
