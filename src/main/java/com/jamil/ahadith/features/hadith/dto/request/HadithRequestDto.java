package com.jamil.ahadith.features.hadith.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.catalog.dto.request.reference.BookReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RawiReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.ExplainingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Size(
            min = 10,
            max = ValidationLimits.HADITH_TEXT_MAX,
            message = "Hadith text must be between {min} and {max} characters"
    )
    private String text;
    @NotNull(message = "Hadith number is required")
    @Positive(message = "Hadith number must be greater than 0")
    private Integer hadithNumber;
    @NotNull(message = "Ruling is required")
    @Valid
    private RulingReferenceRequestDto ruling;
    @NotNull(message = "Rawi is required")
    @Valid
    private RawiReferenceRequestDto rawi;
    @NotNull(message = "Book is required")
    @Valid
    private BookReferenceRequestDto book;
    @Size(max = ValidationLimits.SANAD_MAX)
    private String sanad;
}
