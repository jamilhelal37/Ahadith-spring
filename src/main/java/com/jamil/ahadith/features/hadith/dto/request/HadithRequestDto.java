package com.jamil.ahadith.features.hadith.dto.request;

import com.jamil.ahadith.features.catalog.dto.request.reference.BookReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RawiReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.ExplainingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HadithRequestDto {
    @Valid
    private HadithReferenceRequestDto subValid;
    @Valid
    private ExplainingReferenceRequestDto explaining;
    private String type;
    @NotBlank(message = "Hadith text is required")
    private String text;
    private String normalText;
    private String searchText;
    @NotNull(message = "Hadith number is required")
    private Integer hadithNumber;
    @Valid
    private RulingReferenceRequestDto ruling;
    @Valid
    private RawiReferenceRequestDto rawi;
    @Valid
    private BookReferenceRequestDto book;
    private String sanad;
}
