package com.jamil.ahadith.features.hadith.dto.update;

import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FakeHadithUpdateDto {
    @Valid
    private HadithReferenceRequestDto subValid;
    @Size(max = ValidationLimits.HADITH_TEXT_MAX)
    private String text;
    @Valid
    private RulingReferenceRequestDto ruling;
}
