package com.jamil.ahadith.features.hadith.dto.request;

import com.jamil.ahadith.features.hadith.entity.Hadith;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SimilarAhadithRequestDto {
    @NotNull(message = "Main hadith is required")
    private Hadith mainHadith;
    @NotNull(message = "Similar hadith is required")
    private Hadith simHadith;
}
