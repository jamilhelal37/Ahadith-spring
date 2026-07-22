package com.jamil.ahadith.features.hadith.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.catalog.dto.request.reference.BookReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RawiReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.ExplainingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HadithRequestDto {
    @Valid
    private HadithReferenceRequestDto subValid;
    @Valid
    private ExplainingReferenceRequestDto explaining;
    @NotNull(message = "Hadith type is required")
    private HadithType type;
    @NotBlank(message = "Hadith text is required")
    @Size(max = ValidationLimits.HADITH_TEXT_MAX)
    private String text;
    @NotNull(message = "Hadith number is required")
    @Min(value = 0, message = "Hadith number must be greater than or equal to 0")
    private Integer hadithNumber;
    @Valid
    private RulingReferenceRequestDto ruling;
    @Valid
    private RawiReferenceRequestDto rawi;
    @Valid
    private BookReferenceRequestDto book;
    @Size(max = ValidationLimits.SANAD_MAX)
    private String sanad;
}
