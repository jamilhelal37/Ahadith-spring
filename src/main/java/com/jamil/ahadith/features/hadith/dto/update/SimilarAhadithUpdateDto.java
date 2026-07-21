package com.jamil.ahadith.features.hadith.dto.update;

import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class SimilarAhadithUpdateDto {
    @Valid
    private HadithReferenceRequestDto mainHadith;
    @Valid
    private HadithReferenceRequestDto simHadith;
}
