package com.jamil.ahadith.features.hadith.dto.request;

import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SimilarAhadithRequestDto {
    @Valid
    @NotNull(message = "Main hadith is required")
    private HadithReferenceRequestDto mainHadith;
    @Valid
    @NotNull(message = "Similar hadith is required")
    private HadithReferenceRequestDto simHadith;
}
