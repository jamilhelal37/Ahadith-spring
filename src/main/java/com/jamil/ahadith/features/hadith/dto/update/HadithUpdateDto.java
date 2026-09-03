package com.jamil.ahadith.features.hadith.dto.update;

import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.catalog.dto.request.reference.BookReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RawiReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.ExplainingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HadithUpdateDto {
    @Valid
    private HadithReferenceRequestDto subValid;
    @Valid
    private ExplainingReferenceRequestDto explaining;

    private HadithType type;

    @Pattern(regexp = "(?s).*\\S.*", message = "Hadith text must not be blank")
    @Size(min = 10, max = ValidationLimits.HADITH_TEXT_MAX, message = "Hadith text must be between {min} and {max} characters")
    private String text;

    @Positive(message = "Hadith number must be greater than 0")
    private Integer hadithNumber;

    @Valid
    private RulingReferenceRequestDto ruling;
    @Valid
    private RawiReferenceRequestDto rawi;
    @Valid
    private BookReferenceRequestDto book;

    @Size(max = ValidationLimits.SANAD_MAX, message = "Sanad must not exceed {max} characters")
    private String sanad;
}
