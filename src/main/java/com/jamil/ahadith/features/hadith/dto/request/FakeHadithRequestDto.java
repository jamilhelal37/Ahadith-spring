package com.jamil.ahadith.features.hadith.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FakeHadithRequestDto {
    @Valid
    private HadithReferenceRequestDto subValid;
    @NotBlank(message = "Fake hadith text is required")
    @Size(
            min = 10,
            max = ValidationLimits.HADITH_TEXT_MAX,
            message = "Fake hadith text must be between {min} and {max} characters"
    )
    private String text;

    @NotNull(message = "Ruling is required")
    @Valid
    private RulingReferenceRequestDto ruling;
}
