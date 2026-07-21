package com.jamil.ahadith.features.hadith.dto.request;

import com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FakeHadithRequestDto {
    @Valid
    private HadithReferenceRequestDto subValid;
    @NotBlank(message = "Fake hadith text is required")
    private String text;
    private String normalText;
    private String searchText;
    @Valid
    private RulingReferenceRequestDto ruling;
}
