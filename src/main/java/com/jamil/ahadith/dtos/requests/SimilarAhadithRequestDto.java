package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.Hadith;
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
